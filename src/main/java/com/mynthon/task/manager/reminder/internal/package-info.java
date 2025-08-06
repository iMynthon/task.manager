@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"user.internal","task.internal"},
        type = ApplicationModule.Type.CLOSED
)
package com.mynthon.task.manager.reminder.internal;

import org.springframework.modulith.ApplicationModule;