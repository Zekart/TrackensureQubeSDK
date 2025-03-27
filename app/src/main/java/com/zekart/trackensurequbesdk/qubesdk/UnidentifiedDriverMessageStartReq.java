package com.zekart.trackensurequbesdk.qubesdk;

/**
 * Specifies a request to begin receiving unidentified driver messages
 */
public class UnidentifiedDriverMessageStartReq extends BaseRequest {
    public UnidentifiedDriverMessageStartReq()
    {
        super(RequestType.REQUEST_START_UDEVENTS);
    }
}
