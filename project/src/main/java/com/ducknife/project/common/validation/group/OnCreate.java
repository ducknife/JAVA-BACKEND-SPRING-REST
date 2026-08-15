package com.ducknife.project.common.validation.group;

import jakarta.validation.GroupSequence;

/* Validation groups  */
@GroupSequence({ BasicCheck.class, DbCheck.class })
// Thứ tự chạy các group 
public interface OnCreate {

}
