package jiangxiaopeng.ai.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * 阶段 A：单模块下的分层依赖守护（见 {@code docs/DDD_REFACTORING.md}）。
 * <p>
 * 规则仅约束本工程包名中的分层方向；第三方库（含 Spring）不在此列。
 */
@AnalyzeClasses(
        packages = "jiangxiaopeng.ai",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class LayerDependencyRulesTest {

    @ArchTest
    static final ArchRule domain_must_not_depend_on_infrastructure =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule domain_must_not_depend_on_interfaces =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..interfaces..");

    @ArchTest
    static final ArchRule application_must_not_depend_on_infrastructure =
            noClasses()
                    .that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAPackage("..infrastructure..");
}
