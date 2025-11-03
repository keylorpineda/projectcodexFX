# Atajos para cobertura con JaCoCo

.PHONY: coverage coverage-open

coverage:
	@./mvnw -q -DskipITs -Djacoco.skip=false -Dgpg.skip -T1C test jacoco:report
	@tail -n +2 target/site/jacoco/jacoco.csv | awk -F, '{missI+=$$4; covI+=$$5; missL+=$$8; covL+=$$9} END {printf("\033[1mInstrucciones cubiertas: %.2f%%\033[0m\n\033[1mLíneas cubiertas: %.2f%%\033[0m\n", (covI*100)/(missI+covI), (covL*100)/(missL+covL))}'
	@echo "Reporte HTML: target/site/jacoco/index.html"

coverage-open: coverage
	@command -v open >/dev/null 2>&1 && open target/site/jacoco/index.html || echo "Abra target/site/jacoco/index.html en su navegador"