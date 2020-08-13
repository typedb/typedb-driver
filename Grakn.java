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
import grakn.client.concept.ConceptIID;
import grakn.client.concept.Label;
import grakn.client.concept.ValueType;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.EntityType;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.Rule;
import grakn.client.concept.type.ThingType;
import grakn.client.connection.GraknClient;
import grakn.client.connection.GraknDatabase;
import grakn.client.connection.GraknTransaction;
import grakn.protocol.ConceptProto;
import grakn.protocol.TransactionProto;
import graql.lang.pattern.Pattern;
import graql.lang.query.GraqlCompute;
import graql.lang.query.GraqlDefine;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlQuery;
import graql.lang.query.GraqlUndefine;
import io.grpc.ManagedChannel;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public final class Grakn {

	public static Client client() {
		return new GraknClient();
	}

	public static Client client(String address) {
        return new GraknClient(address, null, null);
    }

    public static Client client(String address, String username, String password) {
        return new GraknClient(address, username, password);
    }

	public interface Client extends AutoCloseable {

		GraknClient overrideChannel(ManagedChannel channel);

		boolean isOpen();

		Session session(String databaseName);

		Session schemaSession(String databaseName);

		Session session(String databaseName, Session.Type type);

		DatabaseManager databases();
	}

	/**
	 * @see Transaction
	 * @see Client
	 */
	public interface Session extends AutoCloseable {

		Transaction.Builder transaction();

		Transaction transaction(Transaction.Type type);

		boolean isOpen();

		Database database();

		enum Type {
            DATA(0),
            SCHEMA(1);

            private final int id;
            private final boolean isSchema;

            Type(int id) {
                this.id = id;
                this.isSchema = id == 1;
            }

            public static Session.Type of(int value) {
                for (Session.Type t : Session.Type.values()) {
                    if (t.id == value) return t;
                }
                return null;
            }

            public boolean isData() { return !isSchema; }

            public boolean isSchema() { return isSchema; }
        }
	}

	public interface Transaction extends AutoCloseable {

		Type type();

		Session session();

		Database database();

		Future<List<ConceptMap>> execute(GraqlDefine query);

		Future<List<ConceptMap>> execute(GraqlUndefine query);

		Future<List<ConceptMap>> execute(GraqlInsert query, QueryOptions options);

		Future<List<ConceptMap>> execute(GraqlInsert query);

		Future<List<Void>> execute(GraqlDelete query, QueryOptions options);

		Future<List<Void>> execute(GraqlDelete query);

		Future<List<ConceptMap>> execute(GraqlGet query, QueryOptions options);

		Future<List<ConceptMap>> execute(GraqlGet query);

		Future<Stream<ConceptMap>> stream(GraqlDefine query);

		Future<Stream<ConceptMap>> stream(GraqlUndefine query);

		Future<Stream<ConceptMap>> stream(GraqlInsert query, QueryOptions options);

		Future<Stream<ConceptMap>> stream(GraqlInsert query);

		Future<Stream<Void>> stream(GraqlDelete query, QueryOptions options);

		Future<Stream<Void>> stream(GraqlDelete query);

		Future<Stream<ConceptMap>> stream(GraqlGet query, QueryOptions options);

		Future<Stream<ConceptMap>> stream(GraqlGet query);

		Future<List<Numeric>> execute(GraqlGet.Aggregate query);

		Future<List<Numeric>> execute(GraqlGet.Aggregate query, QueryOptions options);

		Future<Stream<Numeric>> stream(GraqlGet.Aggregate query);

		Future<Stream<Numeric>> stream(GraqlGet.Aggregate query, QueryOptions options);

		Future<List<AnswerGroup<ConceptMap>>> execute(GraqlGet.Group query);

		Future<List<AnswerGroup<ConceptMap>>> execute(GraqlGet.Group query, QueryOptions options);

		Future<Stream<AnswerGroup<ConceptMap>>> stream(GraqlGet.Group query);

		Future<Stream<AnswerGroup<ConceptMap>>> stream(GraqlGet.Group query, QueryOptions options);

		Future<List<AnswerGroup<Numeric>>> execute(GraqlGet.Group.Aggregate query);

		Future<List<AnswerGroup<Numeric>>> execute(GraqlGet.Group.Aggregate query, QueryOptions options);

		Future<Stream<AnswerGroup<Numeric>>> stream(GraqlGet.Group.Aggregate query);

		Future<Stream<AnswerGroup<Numeric>>> stream(GraqlGet.Group.Aggregate query, QueryOptions options);

		Future<List<Numeric>> execute(GraqlCompute.Statistics query);

		Future<Stream<Numeric>> stream(GraqlCompute.Statistics query);

		Future<List<ConceptList>> execute(GraqlCompute.Path query);

		Future<Stream<ConceptList>> stream(GraqlCompute.Path query);

		Future<List<ConceptSetMeasure>> execute(GraqlCompute.Centrality query);

		Future<Stream<ConceptSetMeasure>> stream(GraqlCompute.Centrality query);

		Future<List<ConceptSet>> execute(GraqlCompute.Cluster query);

		Future<Stream<ConceptSet>> stream(GraqlCompute.Cluster query);

		Future<? extends List<? extends Answer>> execute(GraqlQuery query);

		Future<? extends List<? extends Answer>> execute(GraqlQuery query, QueryOptions options);

		Future<? extends Stream<? extends Answer>> stream(GraqlQuery query);

		Future<? extends Stream<? extends Answer>> stream(GraqlQuery query, QueryOptions options);

		boolean isOpen();

		void commit();

		ThingType.Remote<?, ?> getRootType();

		@Nullable
		ThingType.Remote<?, ?> getThingType(Label label);

		@Nullable
		EntityType.Remote getEntityType(String label);

		@Nullable
		RelationType.Remote getRelationType(String label);

		@Nullable
		AttributeType.Remote<?> getAttributeType(String label);

		@Nullable
		Rule.Remote getRule(String label);

		@Nullable
		grakn.client.concept.type.Type.Remote<?> getType(Label label);

		grakn.client.concept.type.Type.Remote<?> getMetaConcept();

		RelationType.Remote getMetaRelationType();

		RoleType.Remote getMetaRoleType();

		AttributeType.Remote<?> getMetaAttributeType();

		EntityType.Remote getMetaEntityType();

		Rule.Remote getMetaRule();

		@Nullable
		Concept.Remote<?> getConcept(ConceptIID iid);

		EntityType.Remote putEntityType(String label);

		EntityType.Remote putEntityType(Label label);

		<V> AttributeType.Remote<V> putAttributeType(String label, ValueType<V> valueType);

		<V> AttributeType.Remote<V> putAttributeType(Label label, ValueType<V> valueType);

		RelationType.Remote putRelationType(String label);

		RelationType.Remote putRelationType(Label label);

		Rule.Remote putRule(String label, Pattern when, Pattern then);

		Rule.Remote putRule(Label label, Pattern when, Pattern then);

		TransactionProto.Transaction.Res runConceptMethod(ConceptIID iid, ConceptProto.Method.Req method);

		<T> Stream<T> iterateConceptMethod(ConceptIID iid, ConceptProto.Method.Iter.Req method, Function<ConceptProto.Method.Iter.Res, T> responseReader);

		Explanation getExplanation(ConceptMap explainable);

		<T> Stream<T> iterate(TransactionProto.Transaction.Iter.Req request, Function<TransactionProto.Transaction.Iter.Res, T> responseReader);

		interface Builder {

			/**
			 * Read-only transaction, where database mutation is prohibited
			 */
			Transaction read();

			/**
			 * Write transaction, where database mutation is allowed
			 */
			Transaction write();
		}

		interface Option<T> {
        }

		interface QueryOptions {

	        QueryOptions infer(boolean infer);

	        QueryOptions explain(boolean explain);

	        QueryOptions batchSize(int size);

	        QueryOptions batchSize(BatchSize batchSize);

	        <T> QueryOptions set(Option<T> flag, T value);

	        <T> QueryOptions whenSet(Option<T> option, Consumer<T> consumer);
	    }

	    // TODO: align this tremendously awkward interface with core
	    interface Options {
	        QueryOptions DEFAULT = new GraknTransaction.QueryOptionsImpl();

	        static QueryOptions infer(boolean infer) {
	            return DEFAULT.infer(infer);
	        }

	        static QueryOptions explain(boolean explain) {
	            return DEFAULT.explain(explain);
	        }

	        static QueryOptions batchSize(int size) {
	            return DEFAULT.batchSize(size);
	        }

	        static QueryOptions batchSize(BatchSize batchSize) {
	            return DEFAULT.batchSize(batchSize);
	        }
	    }

		enum Type {

			/**
			 * Read-only transaction, where database mutation is prohibited
			 */
	        READ(0),

			/**
			 * Write transaction, where database mutation is allowed
			 */
			WRITE(1);

	        private final int type;

	        Type(int type) {
	            this.type = type;
	        }

	        public int iid() {
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

	    enum BooleanOption implements Option<Boolean> {
	        INFER,
	        EXPLAIN
	    }

	    enum BatchOption implements Option<TransactionProto.Transaction.Iter.Req.Options> {
	        BATCH_SIZE
	    }

	    enum BatchSize {
	        ALL
	    }
	}

	/**
	 * Manages a collection of Grakn databases.
	 */
	public interface DatabaseManager {

		boolean contains(String name);

		void create(String name);

		void delete(String name);

		List<String> all();
	}

	public interface Database extends Serializable {

		@CheckReturnValue
		static Database of(String name) {
		    return new GraknDatabase(name);
		}

		@CheckReturnValue
		String name();
	}
}
