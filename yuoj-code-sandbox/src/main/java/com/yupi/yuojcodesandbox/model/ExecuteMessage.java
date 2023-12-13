package com.yupi.yuojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/8 19:51
 */
@Data
public class ExecuteMessage {

    private int exitValue;

    private String message;

    private String errorMessage;

    private long time;

    private long memory;
}
