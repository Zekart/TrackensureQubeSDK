package com.zekart.trackensurequbesdk.qubesdk;

/**
 * Specifies a request to stop receiving unidentified driver messages
 */
public class UnidentifiedDriverMessageStopReq extends BaseRequest {
    public UnidentifiedDriverMessageStopReq()
    {
        super(RequestType.REQUEST_STOP_UDEVENTS);
    }
}
