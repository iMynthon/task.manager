@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"user.internal","reminder.internal"},
        type = org.springframework.modulith.ApplicationModule.Type.CLOSED
)
package com.mynthon.task.manager.task.internal;