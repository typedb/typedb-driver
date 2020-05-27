package grakn.client;

import grakn.client.answer.Answer;
import grakn.client.answer.AnswerGroup;
import grakn.client.answer.ConceptList;
import grakn.client.answer.ConceptMap;
import grakn.client.answer.ConceptSet;
import grakn.client.answer.ConceptSetMeasure;
import grakn.client.answer.Explanation;
import grakn.client.answer.Numeric;
import grakn.client.answer.Void;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptId;
import grakn.client.concept.DataType;
import grakn.client.concept.Label;
import grakn.client.concept.Rule;
import grakn.client.concept.SchemaConcept;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.EntityType;
import grakn.client.concept.type.MetaType;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.Role;
import grakn.protocol.session.ConceptProto;
import grakn.protocol.session.SessionProto;
import graql.lang.pattern.Pattern;
import graql.lang.query.GraqlCompute;
import graql.lang.query.GraqlDefine;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlQuery;
import graql.lang.query.GraqlUndefine;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Transaction extends AutoCloseable {
    Type type();

    Session session();

    Keyspace keyspace();

    List<ConceptMap> execute(GraqlDefine query);

    List<ConceptMap> execute(GraqlUndefine query);

    List<ConceptMap> execute(GraqlInsert query, boolean infer);

    List<ConceptMap> execute(GraqlInsert query);

    List<Void> execute(GraqlDelete query, boolean infer);

    List<Void> execute(GraqlDelete query);

    List<ConceptMap> execute(GraqlGet query, boolean infer);

    List<ConceptMap> execute(GraqlGet query);

    Stream<ConceptMap> stream(GraqlDefine query);

    Stream<ConceptMap> stream(GraqlUndefine query);

    Stream<ConceptMap> stream(GraqlInsert query, boolean infer);

    Stream<ConceptMap> stream(GraqlInsert query);

    Stream<Void> stream(GraqlDelete query, boolean infer);

    Stream<Void> stream(GraqlDelete query);

    Stream<ConceptMap> stream(GraqlGet query, boolean infer);

    Stream<ConceptMap> stream(GraqlGet query);

    List<Numeric> execute(GraqlGet.Aggregate query);

    List<Numeric> execute(GraqlGet.Aggregate query, boolean infer);

    Stream<Numeric> stream(GraqlGet.Aggregate query);

    Stream<Numeric> stream(GraqlGet.Aggregate query, boolean infer);

    List<AnswerGroup<ConceptMap>> execute(GraqlGet.Group query);

    List<AnswerGroup<ConceptMap>> execute(GraqlGet.Group query, boolean infer);

    Stream<AnswerGroup<ConceptMap>> stream(GraqlGet.Group query);

    Stream<AnswerGroup<ConceptMap>> stream(GraqlGet.Group query, boolean infer);

    List<AnswerGroup<Numeric>> execute(GraqlGet.Group.Aggregate query);

    List<AnswerGroup<Numeric>> execute(GraqlGet.Group.Aggregate query, boolean infer);

    Stream<AnswerGroup<Numeric>> stream(GraqlGet.Group.Aggregate query);

    Stream<AnswerGroup<Numeric>> stream(GraqlGet.Group.Aggregate query, boolean infer);

    List<Numeric> execute(GraqlCompute.Statistics query);

    Stream<Numeric> stream(GraqlCompute.Statistics query);

    List<ConceptList> execute(GraqlCompute.Path query);

    Stream<ConceptList> stream(GraqlCompute.Path query);

    List<ConceptSetMeasure> execute(GraqlCompute.Centrality query);

    Stream<ConceptSetMeasure> stream(GraqlCompute.Centrality query);

    List<ConceptSet> execute(GraqlCompute.Cluster query);

    Stream<ConceptSet> stream(GraqlCompute.Cluster query);

    List<? extends Answer> execute(GraqlQuery query);

    List<? extends Answer> execute(GraqlQuery query, boolean infer);

    Stream<? extends Answer> stream(GraqlQuery query);

    Stream<? extends Answer> stream(GraqlQuery query, boolean infer);

    void close();

    boolean isOpen();

    // TODO remove - backwards compatibility
    boolean isClosed();

    void commit();

    @Nullable
    grakn.client.concept.type.Type.Remote<?, ?> getType(Label label);

    @Nullable
    EntityType.Remote getEntityType(String label);

    @Nullable
    RelationType.Remote getRelationType(String label);

    @SuppressWarnings("unchecked")
    @Nullable
    <V> AttributeType.Remote<V> getAttributeType(String label);

    @Nullable
    Role.Remote getRole(String label);

    @Nullable
    Rule.Remote getRule(String label);

    @SuppressWarnings("unchecked")
    @Nullable
    SchemaConcept.Remote<?> getSchemaConcept(Label label);

    SchemaConcept<?> getMetaConcept();

    MetaType.Remote<?, ?> getMetaRelationType();

    MetaType.Remote<?, ?> getMetaRole();

    MetaType.Remote<?, ?> getMetaAttributeType();

    MetaType.Remote<?, ?> getMetaEntityType();

    MetaType.Remote<?, ?> getMetaRule();

    @Nullable
    Concept.Remote<?> getConcept(ConceptId id);

    @SuppressWarnings("unchecked")
    <V> Collection<Attribute.Remote<V>> getAttributesByValue(V value);

    EntityType.Remote putEntityType(String label);

    EntityType.Remote putEntityType(Label label);

    <V> AttributeType.Remote<V> putAttributeType(String label, DataType<V> dataType);

    @SuppressWarnings("unchecked")
    <V> AttributeType.Remote<V> putAttributeType(Label label, DataType<V> dataType);

    RelationType.Remote putRelationType(String label);

    RelationType.Remote putRelationType(Label label);

    Role.Remote putRole(String label);

    Role.Remote putRole(Label label);

    Rule.Remote putRule(String label, Pattern when, Pattern then);

    Rule.Remote putRule(Label label, Pattern when, Pattern then);

    Stream<SchemaConcept.Remote<?>> sups(SchemaConcept.Remote<?> schemaConcept);

    SessionProto.Transaction.Res runConceptMethod(ConceptId id, ConceptProto.Method.Req method);

    <T> Stream<T> iterateConceptMethod(ConceptId id, ConceptProto.Method.Iter.Req method, Function<ConceptProto.Method.Iter.Res, T> responseReader);

    Explanation getExplanation(ConceptMap explainable);

    <T> Stream<T> iterate(SessionProto.Transaction.Iter.Req request, Function<SessionProto.Transaction.Iter.Res, T> responseReader);

    public enum Type {
        READ(0),  //Read only transaction where mutations to the graph are prohibited
        WRITE(1); //Write transaction where the graph can be mutated

        private final int type;

        Type(int type) {
            this.type = type;
        }

        public int id() {
            return type;
        }

        @Override
        public String toString() {
            return this.name();
        }

        public static Type of(int value) {
            for (Type t : Transaction.Type.values()) {
                if (t.type == value) return t;
            }
            return null;
        }

        public static Type of(String value) {
            for (Type t : Transaction.Type.values()) {
                if (t.name().equalsIgnoreCase(value)) return t;
            }
            return null;
        }
    }

    interface Builder {
        Transaction read();

        Transaction write();
    }
}
