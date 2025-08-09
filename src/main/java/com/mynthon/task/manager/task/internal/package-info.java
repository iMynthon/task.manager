@org.springframework.modulith.ApplicationModule(
        displayName = "Internal task",
        allowedDependencies = {"user.internal","reminder.internal"},
        type = org.springframework.modulith.ApplicationModule.Type.OPEN
)
package com.mynthon.task.manager.task.internal;