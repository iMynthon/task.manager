@org.springframework.modulith.ApplicationModule(
        displayName = "Internal reminder",
        allowedDependencies = {"user.internal","task.internal"},
        type = org.springframework.modulith.ApplicationModule.Type.OPEN
)
package com.mynthon.task.manager.reminder.internal;