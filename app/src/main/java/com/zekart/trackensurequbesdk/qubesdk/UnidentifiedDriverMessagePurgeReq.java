package com.zekart.trackensurequbesdk.qubesdk;

/**
 * Specifies a request to purge unidentified driver messages
 */
public class UnidentifiedDriverMessagePurgeReq extends BaseRequest {
    public UnidentifiedDriverMessagePurgeReq()
    {
        super(RequestType.PURGE_UDEVENTS);
    }
}
