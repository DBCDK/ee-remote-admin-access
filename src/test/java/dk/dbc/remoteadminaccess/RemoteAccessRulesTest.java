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

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@RunWith(Parameterized.class)
public class RemoteAccessRulesTest {

    private static final Logger log = LoggerFactory.getLogger(RemoteAccessRulesTest.class);

    private final String name;
    private final String expected;
    private final String remoteIp;
    private final String xForwardedFor;
    private final String configXForwardedFor;

    public RemoteAccessRulesTest(String name, String expectedIp, String remoteIp, String xForwardedFor, String configXForwardedFor) {
        this.name = name;
        this.expected = expectedIp;
        this.remoteIp = remoteIp;
        this.xForwardedFor = xForwardedFor;
        this.configXForwardedFor = configXForwardedFor;
    }


    @Parameters(name = "{0}")
    public static Collection<Object[]> tests() {
        return Arrays.asList(test("proxy outside allowed", "8.8.8.8", "8.8.8.8", "127.0.0.1, 172.16.0.0", "10.0.0.0/8"),
                             test("no-forwarded-for", "8.8.8.8", "8.8.8.8", null, "10.0.0.0/8"),
                             test("remote-and forward-for in allowed", "127.0.0.1", "172.16.5.5", "127.0.0.1, 172.16.0.0", "172.16.0.0/12"),
                             test("proxy-with non ipv4", "172.16.5.5", "172.16.5.5", "127.0.0.1, 0:0:0:0:0:0:0:1", "172.16.0.0/12"),
                             test("non ipv4", "0:0:0:0:0:0:0:1", "0:0:0:0:0:0:0:1", "127.0.0.1, 172.16.0.0", "172.16.0.0/12"));
    }

    private static Object[] test(Object... objs) {
        return objs;
    }

    @Test
    public void test() throws Exception {
        System.out.println(name);

        RemoteAccessRules remoteAccessRules = new RemoteAccessRules() {
            @Override
            String getEnv(String env) {
                switch (env) {
                    case "X_FORWARDED_FOR":
                        return configXForwardedFor;
                    default:
                        return null;
                }
            }
        };
        remoteAccessRules.init();

        String actual = remoteAccessRules.remoteIp(remoteIp, xForwardedFor);
        log.trace("actual = {}", actual);
        assertEquals(expected, actual);
    }

}
