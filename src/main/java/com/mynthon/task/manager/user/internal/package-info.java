@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"reminder.internal","task.internal"},
        type = org.springframework.modulith.ApplicationModule.Type.CLOSED
)
package com.mynthon.task.manager.user.internal;
