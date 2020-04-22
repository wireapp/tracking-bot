// Wire
// Copyright (C) 2016 Wire Swiss GmbH
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see http://www.gnu.org/licenses/.
//

package com.wire.bots.tracking;

import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.Server;
import com.wire.bots.tracking.DAO.EventsDAO;
import com.wire.bots.tracking.model.Config;
import io.dropwizard.setup.Environment;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

public class Service extends Server<Config> {

    public static void main(String[] args) throws Exception {
        new Service().run(args);
    }

    @Override
    protected MessageHandlerBase createHandler(Config config, Environment env) {
        final EventsDAO eventsDAO = jdbi.onDemand(EventsDAO.class);
        return new MessageHandler(eventsDAO);
    }

    @Override
    protected void initialize(Config config, Environment env) {
        Security.addProvider(new BouncyCastleProvider());
    }
}
