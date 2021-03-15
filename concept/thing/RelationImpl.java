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

package grakn.client.concept.thing;

import grakn.client.api.Transaction;
import grakn.client.api.concept.thing.Relation;
import grakn.client.api.concept.thing.Thing;
import grakn.client.api.concept.type.RoleType;
import grakn.client.common.Proto;
import grakn.client.concept.type.RelationTypeImpl;
import grakn.client.concept.type.RoleTypeImpl;
import grakn.client.concept.type.TypeImpl;
import grakn.common.collection.Bytes;
import grakn.protocol.ConceptProto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static grakn.client.concept.type.RoleTypeImpl.protoRoleTypes;
import static grakn.client.concept.type.TypeImpl.protoTypes;
import static java.util.Arrays.asList;

public class RelationImpl extends ThingImpl implements Relation {

    private final RelationTypeImpl type;

    RelationImpl(String iid, RelationTypeImpl type) {
        super(iid);
        this.type = type;
    }

    public static RelationImpl of(ConceptProto.Thing protoThing) {
        return new RelationImpl(Bytes.bytesToHexString(protoThing.getIid().toByteArray()),
                                RelationTypeImpl.of(protoThing.getType()));
    }

    @Override
    public RelationImpl.Remote asRemote(Transaction transaction) {
        return new RelationImpl.Remote(transaction, getIID(), type);
    }

    @Override
    public RelationTypeImpl getType() {
        return type;
    }

    @Override
    public final RelationImpl asRelation() {
        return this;
    }

    public static class Remote extends ThingImpl.Remote implements Relation.Remote {

        private final RelationTypeImpl type;

        public Remote(Transaction transaction, String iid, RelationTypeImpl type) {
            super(transaction, iid);
            this.type = type;
        }

        @Override
        public RelationImpl.Remote asRemote(Transaction transaction) {
            return new RelationImpl.Remote(transaction, getIID(), type);
        }

        @Override
        public RelationTypeImpl getType() {
            return type;
        }

        @Override
        public Map<RoleTypeImpl, List<ThingImpl>> getPlayersByRoleType() {
            Map<RoleTypeImpl, List<ThingImpl>> rolePlayerMap = new HashMap<>();
            stream(Proto.Thing.Relation.getPlayersByRoleType(getIID()))
                    .flatMap(res -> res.getRelationGetPlayersByRoleTypeResPart().getRoleTypesWithPlayersList().stream())
                    .forEach(rolePlayer -> {
                        RoleTypeImpl role = TypeImpl.of(rolePlayer.getRoleType()).asRoleType();
                        ThingImpl player = ThingImpl.of(rolePlayer.getPlayer());
                        if (rolePlayerMap.containsKey(role)) rolePlayerMap.get(role).add(player);
                        else rolePlayerMap.put(role, new ArrayList<>(Collections.singletonList(player)));
                    });
            return rolePlayerMap;
        }

        @Override
        public Stream<ThingImpl> getPlayers(RoleType... roleTypes) {
            return stream(Proto.Thing.Relation.getPlayers(getIID(), protoTypes(asList(roleTypes))))
                    .flatMap(rp -> rp.getRelationGetPlayersResPart().getThingsList().stream())
                    .map(ThingImpl::of);
        }

        @Override
        public void addPlayer(RoleType roleType, Thing player) {
            execute(Proto.Thing.Relation.addPlayer(getIID(), protoRoleTypes(roleType), Proto.Thing.thing(player.getIID())));
        }

        @Override
        public void removePlayer(RoleType roleType, Thing player) {
            execute(Proto.Thing.Relation.removePlayer(getIID(), protoRoleTypes(roleType), Proto.Thing.thing(player.getIID())));
        }

        @Override
        public final RelationImpl.Remote asRelation() {
            return this;
        }
    }
}