package finalprojectprogramming.project.services.reservation;

import static finalprojectprogramming.project.services.reservation.fixtures.ReservationTestDataFactory.attendeeBuilder;
import static finalprojectprogramming.project.services.reservation.fixtures.ReservationTestDataFactory.checkInRequestBuilder;
import static finalprojectprogramming.project.services.reservation.fixtures.ReservationTestDataFactory.notificationBuilder;
import static finalprojectprogramming.project.services.reservation.fixtures.ReservationTestDataFactory.reservationBuilder;
import static finalprojectprogramming.project.services.reservation.fixtures.ReservationTestDataFactory.reservationDtoBuilder;
import static finalprojectprogramming.project.services.reservation.fixtures.ReservationTestDataFactory.spaceBuilder;
import static finalprojectprogramming.project.services.reservation.fixtures.ReservationTestDataFactory.userBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import finalprojectprogramming.project.dtos.ReservationCheckInRequest;
import finalprojectprogramming.project.dtos.ReservationDTO;
import finalprojectprogramming.project.exceptions.BusinessRuleException;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.Notification;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.ReservationAttendee;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.repositories.ReservationRepository;
import finalprojectprogramming.project.repositories.SpaceRepository;
import finalprojectprogramming.project.repositories.UserRepository;
import finalprojectprogramming.project.security.SecurityUtils;
import finalprojectprogramming.project.services.notification.ReservationNotificationService;
import finalprojectprogramming.project.services.space.SpaceAvailabilityValidator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplementationTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private SpaceAvailabilityValidator availabilityValidator;

    @Mock
    private ReservationNotificationService notificationService;

    private ReservationServiceImplementation service;

    @BeforeEach
    void setUp() {
        service = new ReservationServiceImplementation(
                reservationRepository,
                userRepository,
                spaceRepository,
                new ModelMapper(),
                availabilityValidator,
                notificationService);
    }

    @Test
    void createReservationPersistsEntityAndReturnsDto() {
        ReservationDTO request = reservationDtoBuilder()
                .withUserId(10L)
                .withSpaceId(20L)
                .withQrCode("unique-qr")
                .withStatus(ReservationStatus.CONFIRMED)
                .withStart(LocalDateTime.now().plusHours(2))
                .withEnd(LocalDateTime.now().plusHours(3))
                .withAttendees(2)
                .build();
        User user = userBuilder().withId(10L).build();
        Space space = spaceBuilder().withId(20L).build();
        Notification notification = notificationBuilder().withId(901L).build();
        Reservation saved = reservationBuilder()
                .withId(55L)
                .withUser(user)
                .withSpace(space)
                .withQrCode("unique-qr")
                .withNotifications(List.of(notification))
                .build();
        saved.setAttendeeRecords(new ArrayList<>());

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(spaceRepository.findById(20L)).thenReturn(Optional.of(space));
        when(reservationRepository.findAll()).thenReturn(List.of());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(saved);

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(10L, UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            ReservationDTO response = service.create(request);

            assertThat(response.getId()).isEqualTo(55L);
            assertThat(response.getUserId()).isEqualTo(10L);
            assertThat(response.getSpaceId()).isEqualTo(20L);
            assertThat(response.getQrCode()).isEqualTo("unique-qr");
            assertThat(response.getNotificationIds()).containsExactly(901L);
            assertThat(response.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);

            ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
            verify(reservationRepository).save(reservationCaptor.capture());
            Reservation persisted = reservationCaptor.getValue();
            assertThat(persisted.getUser()).isEqualTo(user);
            assertThat(persisted.getSpace()).isEqualTo(space);
            assertThat(persisted.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
            assertThat(persisted.getCreatedAt()).isNotNull();
            assertThat(persisted.getUpdatedAt()).isNotNull();
            assertThat(persisted.getNotifications()).isNotNull();
            assertThat(persisted.getAttendeeRecords()).isNotNull();

            mocked.verify(() -> SecurityUtils.requireSelfOrAny(10L, UserRole.SUPERVISOR, UserRole.ADMIN));
            verify(availabilityValidator).assertAvailability(space, request.getStartTime(), request.getEndTime(), null);
            verify(notificationService).notifyReservationCreated(saved);
        }
    }

    @Test
    void createReservationRejectsDuplicatedQrCode() {
        ReservationDTO request = reservationDtoBuilder().withQrCode("duplicate").build();
        Reservation existing = reservationBuilder().withQrCode("duplicate").withDeletedAt(null).build();
        when(userRepository.findById(request.getUserId())).thenReturn(Optional.of(userBuilder().build()));
        when(spaceRepository.findById(request.getSpaceId())).thenReturn(Optional.of(spaceBuilder().build()));
        when(reservationRepository.findAll()).thenReturn(List.of(existing));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(request.getUserId(), UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("QR code is already associated");
        }

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void updateReservationChangesFieldsAndValidatesSecurity() {
        User owner = userBuilder().withId(101L).build();
        Space originalSpace = spaceBuilder().withId(201L).build();
        Space newSpace = spaceBuilder().withId(202L).build();
        Reservation existing = reservationBuilder()
                .withId(900L)
                .withUser(owner)
                .withSpace(originalSpace)
                .withQrCode("initial-qr")
                .withStart(LocalDateTime.now().plusHours(2))
                .withEnd(LocalDateTime.now().plusHours(3))
                .build();
        existing.setAttendees(2);
        existing.setAttendeeRecords(new ArrayList<>());

        ReservationDTO updateRequest = reservationDtoBuilder()
                .withUserId(owner.getId())
                .withSpaceId(newSpace.getId())
                .withStart(LocalDateTime.now().plusHours(4))
                .withEnd(LocalDateTime.now().plusHours(5))
                .withQrCode("updated-qr")
                .withAttendees(3)
                .build();

        when(reservationRepository.findById(900L)).thenReturn(Optional.of(existing));
        when(spaceRepository.findById(newSpace.getId())).thenReturn(Optional.of(newSpace));
        when(reservationRepository.findAll()).thenReturn(List.of());
        when(reservationRepository.save(existing)).thenReturn(existing);

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(owner.getId()), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            ReservationDTO response = service.update(900L, updateRequest);

            assertThat(response.getSpaceId()).isEqualTo(newSpace.getId());
            assertThat(response.getStartTime()).isEqualTo(updateRequest.getStartTime());
            assertThat(response.getEndTime()).isEqualTo(updateRequest.getEndTime());
            assertThat(existing.getQrCode()).isEqualTo("updated-qr");

            verify(availabilityValidator).assertAvailability(newSpace, updateRequest.getStartTime(),
                    updateRequest.getEndTime(), existing.getId());
            mocked.verify(() -> SecurityUtils.requireSelfOrAny(owner.getId(), UserRole.SUPERVISOR, UserRole.ADMIN));
        }
    }

    @Test
    void findByIdRequiresOwnershipAndReturnsDto() {
        User owner = userBuilder().withId(400L).build();
        Reservation reservation = reservationBuilder().withUser(owner).withId(501L).build();
        when(reservationRepository.findById(501L)).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(owner.getId()), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            ReservationDTO response = service.findById(501L);
            assertThat(response.getId()).isEqualTo(501L);
            assertThat(response.getUserId()).isEqualTo(owner.getId());
            mocked.verify(() -> SecurityUtils.requireSelfOrAny(owner.getId(), UserRole.SUPERVISOR, UserRole.ADMIN));
        }
    }

    @Test
    void findAllRequiresSupervisorRoleAndFiltersDeleted() {
        Reservation active = reservationBuilder().withId(10L).withDeletedAt(null).build();
        Reservation deleted = reservationBuilder().withId(11L).withDeletedAt(LocalDateTime.now()).build();
        when(reservationRepository.findAll()).thenReturn(List.of(active, deleted));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            List<ReservationDTO> results = service.findAll();
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(10L);
            mocked.verify(() -> SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN));
        }
    }

    @Test
    void findByUserFiltersByUserId() {
        User owner = userBuilder().withId(12L).build();
        Reservation matching = reservationBuilder().withUser(owner).build();
        Reservation other = reservationBuilder().withUser(userBuilder().withId(999L).build()).build();
        when(reservationRepository.findAll()).thenReturn(List.of(matching, other));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(owner.getId()), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            List<ReservationDTO> results = service.findByUser(owner.getId());
            assertThat(results).extracting(ReservationDTO::getUserId).containsExactly(owner.getId());
        }
    }

    @Test
    void findBySpaceRequiresSupervisorAndFiltersBySpace() {
        Space target = spaceBuilder().withId(321L).build();
        Reservation matching = reservationBuilder().withSpace(target).build();
        Reservation other = reservationBuilder().withSpace(spaceBuilder().withId(999L).build()).build();
        when(reservationRepository.findAll()).thenReturn(List.of(matching, other));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            List<ReservationDTO> results = service.findBySpace(target.getId());
            assertThat(results).extracting(ReservationDTO::getSpaceId).containsExactly(target.getId());
        }
    }

    @Test
    void cancelReservationSetsStatusAndNotifies() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .withStart(LocalDateTime.now().plusHours(2))
                .build();
        reservation.setAttendeeRecords(new ArrayList<>());
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        Long ownerId = reservation.getUser() != null ? reservation.getUser().getId() : null;
        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(ownerId, UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            ReservationDTO result = service.cancel(reservation.getId(), "reason");
            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELED);
            assertThat(reservation.getCancellationReason()).isEqualTo("reason");
            verify(notificationService).notifyReservationCanceled(reservation);
        }
    }

    @Test
    void cancelReservationRejectsPastStartTime() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .withStart(LocalDateTime.now().minusHours(1))
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        Long ownerId = reservation.getUser() != null ? reservation.getUser().getId() : null;
        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(ownerId, UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            assertThatThrownBy(() -> service.cancel(reservation.getId(), "late"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("cannot be canceled after");
        }
    }

    @Test
    void approveReservationTransitionsToConfirmed() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.PENDING)
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(userRepository.findById(999L)).thenReturn(Optional.of(userBuilder().withId(999L).build()));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            ReservationDTO result = service.approve(reservation.getId(), 999L);
            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
            verify(notificationService).notifyReservationApproved(reservation);
        }
    }

    @Test
    void approveReservationRejectsNonPendingStatus() {
        Reservation reservation = reservationBuilder().withStatus(ReservationStatus.CONFIRMED).build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            assertThatThrownBy(() -> service.approve(reservation.getId(), 1L))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Only pending");
        }
    }

    @Test
    void markCheckInRegistersAttendeeWithinWindow() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .withStart(LocalDateTime.now().minusMinutes(5))
                .withEnd(LocalDateTime.now().plusHours(1))
                .withQrCode("QR-CHECK")
                .withAttendees(2)
                .build();
        reservation.setAttendeeRecords(new ArrayList<>());
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            ReservationCheckInRequest request = checkInRequestBuilder()
                    .withQrCode("QR-CHECK")
                    .withFirstName("Alice")
                    .withLastName("Brown")
                    .withAttendeeId("ID-1")
                    .build();

            ReservationDTO result = service.markCheckIn(reservation.getId(), request);

            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CHECKED_IN);
            assertThat(reservation.getAttendeeRecords()).hasSize(1);
            ReservationAttendee attendee = reservation.getAttendeeRecords().get(0);
            assertThat(attendee.getIdNumber()).isEqualTo("ID-1");
            verify(reservationRepository).save(reservation);
        }
    }

    @Test
    void markCheckInRejectsPendingReservation() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.PENDING)
                .withStart(LocalDateTime.now().plusHours(1))
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            ReservationCheckInRequest request = checkInRequestBuilder().build();
            assertThatThrownBy(() -> service.markCheckIn(reservation.getId(), request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("QR code is not yet active");
        }
    }

    @Test
    void markNoShowTransitionsFromConfirmed() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .withStart(LocalDateTime.now().minusHours(2))
                .build();
        reservation.setCheckinAt(null);
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            ReservationDTO result = service.markNoShow(reservation.getId());
            assertThat(result.getStatus()).isEqualTo(ReservationStatus.NO_SHOW);
        }
    }

    @Test
    void markNoShowRejectsFutureStartTime() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .withStart(LocalDateTime.now().plusHours(2))
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            assertThatThrownBy(() -> service.markNoShow(reservation.getId()))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("after the reservation start time");
        }
    }

    @Test
    void deleteSoftDeletesReservationBasedOnStatus() {
        Reservation canceled = reservationBuilder()
                .withStatus(ReservationStatus.CANCELED)
                .withUser(userBuilder().withId(555L).build())
                .build();
        when(reservationRepository.findById(canceled.getId())).thenReturn(Optional.of(canceled));
        when(reservationRepository.save(canceled)).thenReturn(canceled);

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(555L), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            service.delete(canceled.getId());
            assertThat(canceled.getDeletedAt()).isNotNull();
            mocked.verify(() -> SecurityUtils.requireSelfOrAny(555L, UserRole.SUPERVISOR, UserRole.ADMIN));
        }

        Reservation active = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .withUser(userBuilder().withId(111L).build())
                .build();
        when(reservationRepository.findById(active.getId())).thenReturn(Optional.of(active));
        when(reservationRepository.save(active)).thenReturn(active);

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            service.delete(active.getId());
            mocked.verify(() -> SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN));
        }
    }

    @Test
    void hardDeleteRemovesReservationWhenStatusAllows() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.CANCELED)
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(eq(UserRole.ADMIN))).thenAnswer(invocation -> null);

            service.hardDelete(reservation.getId());
            verify(reservationRepository).delete(reservation);
        }
    }

    @Test
    void hardDeleteRejectsWhenStatusIsNotFinal() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(UserRole.ADMIN)).thenAnswer(invocation -> null);

            assertThatThrownBy(() -> service.hardDelete(reservation.getId()))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Only reservations with CHECKED_IN, NO_SHOW or CANCELED status");
            verify(reservationRepository, never()).delete(any());
        }
    }

    @Test
    void findByIdThrowsWhenReservationDeleted() {
        Reservation reservation = reservationBuilder()
                .withDeletedAt(LocalDateTime.now())
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        Long deletedOwnerId = reservation.getUser() != null ? reservation.getUser().getId() : null;
        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(deletedOwnerId, UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            assertThatThrownBy(() -> service.findById(reservation.getId()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Test
    void createFailsWhenUserIdMissing() {
        ReservationDTO request = reservationDtoBuilder().withUserId(null).build();
        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(null, UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);
            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("User identifier is required");
        }
    }

    @Test
    void createFailsWhenSpaceNotAvailable() {
        ReservationDTO request = reservationDtoBuilder().build();
        when(userRepository.findById(request.getUserId())).thenReturn(Optional.of(userBuilder().build()));
        when(spaceRepository.findById(request.getSpaceId())).thenReturn(Optional.of(spaceBuilder().withActive(false).build()));
        when(reservationRepository.findAll()).thenReturn(List.of());

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(request.getUserId(), UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Space is not available");
        }
    }

    @Test
    void markCheckInRejectsDuplicateAttendee() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .withStart(LocalDateTime.now().minusMinutes(5))
                .withQrCode("QR-CHECK")
                .withAttendeeRecords(List.of(attendeeBuilder().withIdNumber("DUP").build()))
                .withAttendees(1)
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            ReservationCheckInRequest request = checkInRequestBuilder().withQrCode("QR-CHECK").withAttendeeId("DUP").build();
            assertThatThrownBy(() -> service.markCheckIn(reservation.getId(), request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("already been registered");
        }
    }

    @Test
    void toDtoOrdersAttendeeRecordsByCheckInTime() {
        ReservationAttendee first = attendeeBuilder().withId(1L).withCheckInAt(LocalDateTime.now().minusMinutes(5)).build();
        ReservationAttendee second = attendeeBuilder().withId(2L).withCheckInAt(LocalDateTime.now().minusMinutes(2)).build();
        Reservation reservation = reservationBuilder()
                .withAttendeeRecords(List.of(second, first))
                .withNotifications(List.of(notificationBuilder().withId(7L).build()))
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        Long orderingOwnerId = reservation.getUser() != null ? reservation.getUser().getId() : null;
        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(orderingOwnerId, UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            ReservationDTO dto = service.findById(reservation.getId());
            assertThat(dto.getAttendeeRecords())
                    .extracting(record -> tuple(record.getId(), record.getCheckInAt()))
                    .containsExactly(tuple(1L, first.getCheckInAt()), tuple(2L, second.getCheckInAt()));
        }
    }
}
