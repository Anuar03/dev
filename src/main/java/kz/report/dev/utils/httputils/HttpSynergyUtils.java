package kz.report.dev.utils.httputils;

import kz.arta.synergy.forms.common.object.ASFDataWrapperExt;
import kz.arta.synergy.forms.common.util.rest.operations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpSynergyUtils extends ApiOperation {

    public HttpSynergyUtils(String address, String auth) {
        super(address, auth);
    }

    public ASFDataWrapperExt getRegData(String dataUUID) throws IOException {
        return this.getRegData(dataUUID, 0);
    }

    public ASFDataWrapperExt getRegData(String dataUUID, int version) throws IOException {
        HttpURLConnection connection = this.openGetConnection(new URL(this.address + "/rest/api/asforms/data/" + dataUUID + "?version=" + version), this.auth);
        return this.readResult(connection, ASFDataWrapperExt.class);
    }


}
