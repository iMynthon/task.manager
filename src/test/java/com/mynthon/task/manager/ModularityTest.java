package com.mynthon.task.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

@SpringBootTest
public class ModularityTest {

    @Test
    void printModuleNames() {
        ApplicationModules modules = ApplicationModules.of(TaskManagerApplication.class);
        modules.forEach(module ->
                System.out.println(module.getName() + " -> " + module.getDisplayName())
        );
    }


    @Test
    void verifyModularity() {
        ApplicationModules modules = ApplicationModules.of(TaskManagerApplication.class);
        modules.forEach(System.out::println);
        modules.verify();

        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }
}
