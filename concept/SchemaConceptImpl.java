package grakn.client.concept;

import grakn.client.GraknClient;
import grakn.client.exception.GraknClientException;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.session.ConceptProto;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public abstract class SchemaConceptImpl {

    public abstract static class Local<
            SomeSchemaConcept extends SchemaConcept<SomeSchemaConcept>>
            extends ConceptImpl.Local<SomeSchemaConcept>
            implements SchemaConcept.Local<SomeSchemaConcept> {

        private final Label label;

        protected Local(ConceptProto.Concept concept) {
            super(concept);
            this.label = Label.of(concept.getLabelRes().getLabel());
        }

        @Override
        public final Label label() {
            return label;
        }
    }

    public abstract static class Remote<
            BaseType extends SchemaConcept<BaseType>>
            extends ConceptImpl.Remote<BaseType>
            implements SchemaConcept.Remote<BaseType> {

        public Remote(GraknClient.Transaction tx, ConceptId id) {
            super(tx, id);
        }

        public final SchemaConcept.Remote<BaseType> sup(SchemaConcept<?> type) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setSchemaConceptSetSupReq(ConceptProto.SchemaConcept.SetSup.Req.newBuilder()
                                                       .setSchemaConcept(RequestBuilder.ConceptMessage.from(type))).build();

            runMethod(method);
            return this;
        }

        @Override
        public final Label label() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setSchemaConceptGetLabelReq(ConceptProto.SchemaConcept.GetLabel.Req.getDefaultInstance()).build();

            return Label.of(runMethod(method).getSchemaConceptGetLabelRes().getLabel());
        }

        @Override
        public final Boolean isImplicit() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setSchemaConceptIsImplicitReq(ConceptProto.SchemaConcept.IsImplicit.Req.getDefaultInstance()).build();

            return runMethod(method).getSchemaConceptIsImplicitRes().getImplicit();
        }

        @Override
        public SchemaConcept.Remote<BaseType> label(Label label) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setSchemaConceptSetLabelReq(ConceptProto.SchemaConcept.SetLabel.Req.newBuilder()
                                                         .setLabel(label.getValue())).build();

            runMethod(method);
            return this;
        }

        @SuppressWarnings("unchecked")
        @Nullable
        public SchemaConcept.Remote<BaseType> sup() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setSchemaConceptGetSupReq(ConceptProto.SchemaConcept.GetSup.Req.getDefaultInstance()).build();

            ConceptProto.SchemaConcept.GetSup.Res response = runMethod(method).getSchemaConceptGetSupRes();

            switch (response.getResCase()) {
                case NULL:
                    return null;
                case SCHEMACONCEPT:
                    return (SchemaConcept.Remote<BaseType>) Concept.Remote.of(response.getSchemaConcept(), tx()).asSchemaConcept();
                default:
                    throw GraknClientException.unreachableStatement("Unexpected response " + response);
            }

        }

        @Override
        public Stream<? extends SchemaConcept.Remote<BaseType>> sups() {
            return tx().sups(this).filter(this::equalsCurrentBaseType).map(this::asCurrentBaseType);
        }

        @Override
        public Stream<? extends SchemaConcept.Remote<BaseType>> subs() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setSchemaConceptSubsIterReq(ConceptProto.SchemaConcept.Subs.Iter.Req.getDefaultInstance()).build();

            return conceptStream(method, res -> res.getSchemaConceptSubsIterRes().getSchemaConcept()).map(this::asCurrentBaseType);
        }

        @Override
        protected abstract SchemaConcept.Remote<BaseType> asCurrentBaseType(Concept.Remote<?> other);

        protected abstract boolean equalsCurrentBaseType(Concept.Remote<?> other);
    }
}
