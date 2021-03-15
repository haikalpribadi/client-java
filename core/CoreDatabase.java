/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package grakn.client.core;

import grakn.client.api.database.Database;
import grakn.client.common.Proto;
import grakn.protocol.GraknGrpc;

import static grakn.client.core.CoreDatabaseManager.nonNull;

public class CoreDatabase implements Database {

    private final String name;
    private final CoreClient client;
    private final GraknGrpc.GraknBlockingStub blockingGrpcStub;

    public CoreDatabase(CoreDatabaseManager databaseManager, String name) {
        this.name = nonNull((name));
        this.client = databaseManager.client();
        this.blockingGrpcStub = databaseManager.blockingGrpcStub();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void delete() {
        client.call(() -> blockingGrpcStub.databaseDelete(Proto.Database.delete(name)));
    }

    @Override
    public String toString() {
        return name;
    }
}