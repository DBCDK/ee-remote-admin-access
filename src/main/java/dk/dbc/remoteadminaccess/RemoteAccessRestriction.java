/*
 * Copyright (C) 2019 DBC A/S (http://dbc.dk/)
 *
 * This is part of ee-remote-admin-access
 *
 * ee-remote-admin-access is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ee-remote-admin-access is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.remoteadminaccess;

import java.io.IOException;
import java.lang.reflect.Method;
import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

/**
 * Request filter checking for the presence of @{@link RequiresAdmin} annotation
 * on both method and class level. Then it enforces the
 * {@link RemoteAccessRules} rules
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Provider
public class RemoteAccessRestriction implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RemoteAccessRestriction.class);

    // https://stackoverflow.com/questions/28280491/how-to-get-ip-from-containerrequestcontext-jersey-2-1
    @Context
    private HttpServletRequest servletRequest;

    // https://stackoverflow.com/questions/31974857/jersey-2-containerrequestfilter-get-method-annotation
    @Context
    private ResourceInfo resourceInfo;

    @EJB
    private RemoteAccessRules remoteAccess;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Method method = resourceInfo.getResourceMethod();
        Class<?> clazz = method.getDeclaringClass();
        if (method.getAnnotation(RequiresAdmin.class) == null &&
            clazz.getAnnotation(RequiresAdmin.class) == null)
            return;
        String ip = remoteAccess.remoteIp(
                servletRequest.getRemoteAddr(),
                requestContext.getHeaderString("x-forwarded-for"));
        log.trace("ip = {}", ip);
        if (remoteAccess.isAdminIp(ip)) {
            log.info("Granted access for {} to {}", ip, requestContext.getUriInfo().getRequestUri());
        } else {
            log.info("Rejected access for {} to {}", ip, requestContext.getUriInfo().getRequestUri());
            requestContext.abortWith(
                    Response.status(FORBIDDEN)
                            .entity("Not authorized for admin access")
                            .build());
        }
    }
}
