package rnfive.htfu.temperatureregulator.define;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Response {
    private int code;
    private String type;
    private String value;
    private String error;
    private Status status = new Status();

    public Response() {}
}
