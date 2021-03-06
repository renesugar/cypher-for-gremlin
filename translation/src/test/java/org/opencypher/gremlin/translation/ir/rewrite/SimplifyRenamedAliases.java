/*
 * Copyright (c) 2018 "Neo4j, Inc." [https://neo4j.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opencypher.gremlin.translation.ir.rewrite;

import static org.opencypher.gremlin.translation.CypherAstWrapper.parse;
import static org.opencypher.gremlin.translation.Tokens.NULL;
import static org.opencypher.gremlin.translation.helpers.CypherAstAssert.P;
import static org.opencypher.gremlin.translation.helpers.CypherAstAssert.__;
import static org.opencypher.gremlin.translation.helpers.CypherAstAssertions.assertThat;
import static org.opencypher.gremlin.translation.helpers.ScalaHelpers.seq;

import org.junit.Test;
import org.opencypher.gremlin.translation.translator.TranslatorFlavor;

public class SimplifyRenamedAliases {

    private final TranslatorFlavor flavor = new TranslatorFlavor(
        seq(
            InlineMapTraversal$.MODULE$
        ),
        seq()
    );

    @Test
    public void whereMatchPattern() {
        assertThat(parse(
            "MATCH (n) " +
                "WHERE (n)-->(:L) " +
                "RETURN n"
        ))
            .withFlavor(flavor)
            .rewritingWith(SimplifyRenamedAliases$.MODULE$)
            .removes(
                __().V().as("  GENERATED1")
                    .where(__().select("  GENERATED1").where(P.isEq("n")))
                    .as("  cypher.path.start.GENERATED2"))
            .adds(
                __().select("n")
                    .as("  cypher.path.start.GENERATED2")
            );
    }

    @Test
    public void whereNonStartMatchPattern() {
        assertThat(parse(
            "MATCH (n)-->(m) " +
                "WHERE (m)-->(:L) " +
                "RETURN m"
        ))
            .withFlavor(flavor)
            .rewritingWith(SimplifyRenamedAliases$.MODULE$)
            .removes(
                __().V().as("  GENERATED2")
                    .where(__().select("  GENERATED2").where(P.isEq("m")))
                    .as("  cypher.path.start.GENERATED3"))
            .adds(
                __().select("m").is(P.neq(NULL))
                    .as("  cypher.path.start.GENERATED3"));
    }
}
