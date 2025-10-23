package com.municipal.reservationsfx.ui.controllers;

public interface DashboardSubview {

    /**
     * Called every time the view is shown in the admin dashboard container.
     *
     * @param host controller hosting the view
     */
    void onDisplay(AdminDashboardController host);
}
