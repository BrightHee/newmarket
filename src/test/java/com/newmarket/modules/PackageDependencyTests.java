package com.newmarket.modules;

import com.newmarket.Application;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packagesOf = Application.class)
public class PackageDependencyTests {

    private static final String ACCOUNT = "..modules.account..";
    private static final String AREA = "..modules.area..";
    private static final String CHAT_ROOM = "..modules.chatRoom..";
    private static final String GARMENT = "..modules.garment..";
    private static final String MAIN = "..modules.main..";
    private static final String NOTIFICATION = "..modules.notification..";

    @ArchTest
    ArchRule modulesPackageRule = classes().that().resideInAnyPackage("com.newmarket.modules..")
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage("com.newmarket.modules..");

    @ArchTest
    ArchRule accountPackageRule = classes().that().resideInAnyPackage(ACCOUNT)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(ACCOUNT, GARMENT, CHAT_ROOM, NOTIFICATION, MAIN);

    @ArchTest
    ArchRule garmentPackageRule = classes().that().resideInAnyPackage(GARMENT)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(GARMENT, CHAT_ROOM, MAIN)
            .andShould().accessClassesThat()
            .resideInAnyPackage(GARMENT, ACCOUNT, AREA);

    @ArchTest
    ArchRule chatRoomPackageRule = classes().that().resideInAnyPackage(CHAT_ROOM)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(CHAT_ROOM)
            .andShould().accessClassesThat()
            .resideInAnyPackage(CHAT_ROOM, GARMENT, ACCOUNT);

    @ArchTest
    ArchRule notificationPackageRule = classes().that().resideInAnyPackage(NOTIFICATION)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(NOTIFICATION, CHAT_ROOM)
            .andShould().accessClassesThat()
            .resideInAnyPackage(NOTIFICATION, ACCOUNT);;

    @ArchTest
    ArchRule cycleCheck = slices().matching("com.newmarket.modules.(*)..")
            .should().beFreeOfCycles();

}
