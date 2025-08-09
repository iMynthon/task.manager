@org.springframework.modulith.ApplicationModule(
        displayName = "Internal user",
        allowedDependencies = {"reminder.internal","task.internal"},
        type = org.springframework.modulith.ApplicationModule.Type.OPEN
)
package com.mynthon.task.manager.user.internal;
