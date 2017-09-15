/*
    This file is part of Cyclos (www.cyclos.org).
    A project of the Social Trade Organisation (www.socialtrade.org).

    Cyclos is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Cyclos is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cyclos; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 */
package nl.strohalm.cyclos.webservices.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.services.access.ChannelService;
import nl.strohalm.cyclos.services.accounts.external.ExternalTransferService;
import nl.strohalm.cyclos.services.settings.SettingsService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.utils.Pair;
import nl.strohalm.cyclos.webservices.model.ServerErrorVO;

/**
 * Base class for REST controllers
 *
 * @author luis
 */
public abstract class BaseRestController {

    private static final Log LOG = LogFactory.getLog(BaseRestController.class);

    protected SettingsService settingsService;
    protected ExternalTransferService externalTransferService;
    protected ChannelService channelService;

    @Inject
    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

    @Inject
    public void setExternalTransferService(ExternalTransferService externalTransferService) {
        this.externalTransferService = externalTransferService;
    }

    @Inject
    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    /**
     * Handles {@link Exception}s
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ServerErrorVO handleUnknownException(final Exception ex, final HttpServletResponse response) throws IOException {
        Pair<ServerErrorVO, Integer> error = RestHelper.resolveError(ex);
        int errorCode = error.getSecond();
        if (errorCode == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
            LOG.error("Error on REST call", ex);
        }
        response.setStatus(errorCode);
        return error.getFirst();
    }

}
