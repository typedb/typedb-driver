package com.typedb.driver.analyze;

import com.typedb.driver.api.concept.type.EntityType;
import com.typedb.driver.common.NativeIterator;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.exception.TypeDBDriverException;
import com.typedb.driver.jni.Variable;
import com.typedb.driver.jni.typedb_driver;

import java.util.stream.Stream;

import static com.typedb.driver.common.exception.ErrorMessage.Analyze.INVALID_CONSTRAINT_CASTING;
import static com.typedb.driver.common.util.Objects.className;

public abstract class Constraint extends NativeObject<com.typedb.driver.jni.ConstraintWithSpan> {
    public Constraint(com.typedb.driver.jni.ConstraintWithSpan nativeObject) {
        super(nativeObject);
    }

    public static Constraint of(com.typedb.driver.jni.ConstraintWithSpan constraint) {
        com.typedb.driver.jni.ConstraintVariant variant =
                com.typedb.driver.jni.typedb_driver.constraint_variant(constraint);
        switch (variant) {
            case Isa:
                return new Constraint.Isa(constraint);
            case Has:
                return new Constraint.Has(constraint);
            case Links:
                return new Constraint.Links(constraint);
            case Sub:
                return new Constraint.Sub(constraint);
            case Owns:
                return new Constraint.Owns(constraint);
            case Relates:
                return new Constraint.Relates(constraint);
            case Plays:
                return new Constraint.Plays(constraint);
            case FunctionCall:
                return new Constraint.FunctionCall(constraint);
            case Expression:
                return new Constraint.Expression(constraint);
            case Is:
                return new Constraint.Is(constraint);
            case Iid:
                return new Constraint.Iid(constraint);
            case Comparison:
                return new Constraint.Comparison(constraint);
            case KindOf:
                return new Constraint.Kind(constraint);
            case Label:
                return new Constraint.Label(constraint);
            case Value:
                return new Constraint.Value(constraint);
            case Or:
                return new Constraint.Or(constraint);
            case Not:
                return new Constraint.Not(constraint);
            case Try:
                return new Constraint.Try(constraint);
            default:
                throw new IllegalStateException("Unexpected constraint variant: " + variant);
        }
    }

    public com.typedb.driver.jni.ConstraintVariant variant() {
        return com.typedb.driver.jni.typedb_driver.constraint_variant(nativeObject);
    }

    public boolean isIsa() {
        return false;
    }

    public boolean isHas() {
        return false;
    }

    public boolean isLinks() {
        return false;
    }

    public boolean isSub() {
        return false;
    }

    public boolean isOwns() {
        return false;
    }

    public boolean isRelates() {
        return false;
    }

    public boolean isPlays() {
        return false;
    }

    public boolean isFunctionCall() {
        return false;
    }

    public boolean isExpression() {
        return false;
    }

    public boolean isIs() {
        return false;
    }

    public boolean isIid() {
        return false;
    }

    public boolean isComparison() {
        return false;
    }

    public boolean isKindOf() {
        return false;
    }

    public boolean isLabel() {
        return false;
    }

    public boolean isValue() {
        return false;
    }

    public boolean isOr() {
        return false;
    }

    public boolean isNot() {
        return false;
    }

    public boolean isTry() {
        return false;
    }

    public Isa asIsa() {
        throw new TypeDBDriverException(INVALID_CONSTRAINT_CASTING, className(this.getClass()), className(Isa.class));
    }

    public Has asHas() {
        throw new TypeDBDriverException(INVALID_CONSTRAINT_CASTING, className(this.getClass()), className(Has.class));
    }

    public Links asLinks() {
        throw new TypeDBDriverException(INVALID_CONSTRAINT_CASTING, className(this.getClass()), className(Links.class));
    }

    public Sub asSub() {
        throw new TypeDBDriverException(INVALID_CONSTRAINT_CASTING, className(this.getClass()), className(Sub.class));
    }

    public Owns asOwns() {
        throw new TypeDBDriverException(INVALID_CONSTRAINT_CASTING, className(this.getClass()), className(Owns.class));
    }

    public Relates asRelates() {
        throw new TypeDBDriverException(INVALID_CONSTRAINT_CASTING, className(this.getClass()), className(Relates.class));
    }

    public Plays asPlays() {
        throw new TypeDBDriverException(INVALID_CONSTRAINT_CASTING, className(this.getClass()), className(Plays.class));
    }

    public FunctionCall asFunctionCall() {
        throw new TypeDBDriverException(INVALID_CONSTRAINT_CASTING, className(this.getClass()), className(FunctionCall.class));
    }

    public Expression asExpression() {
        throw new TypeDBDriverException(INVALID_CONSTRAINT_CASTING, className(this.getClass()), className(Expression.class));
    }

    public Is asIs() {
        throw new TypeDBDriverException(INVALID_CONSTRAINT_CASTING, className(this.getClass()), className(Is.class));
    }

    public Iid asIid() {
        throw new TypeDBDriverException(INVALID_CONSTRAINT_CASTING, className(this.getClass()), className(Iid.class));
    }

    public Comparison asComparison() {
        throw new TypeDBDriverException(INVALID_CONSTRAINT_CASTING, className(this.getClass()), className(Comparison.class));
    }

    public Kind asKindOf() {
        throw new TypeDBDriverException(INVALID_CONSTRAINT_CASTING, className(this.getClass()), className(Kind.class));
    }

    public Label asLabel() {
        throw new TypeDBDriverException(INVALID_CONSTRAINT_CASTING, className(this.getClass()), className(Label.class));
    }

    public Value asValue() {
        throw new TypeDBDriverException(INVALID_CONSTRAINT_CASTING, className(this.getClass()), className(Value.class));
    }

    public Or asOr() {
        throw new TypeDBDriverException(INVALID_CONSTRAINT_CASTING, className(this.getClass()), className(Or.class));
    }

    public Not asNot() {
        throw new TypeDBDriverException(INVALID_CONSTRAINT_CASTING, className(this.getClass()), className(Not.class));
    }

    public Try asTry() {
        throw new TypeDBDriverException(INVALID_CONSTRAINT_CASTING, className(this.getClass()), className(Try.class));
    }

    public static class Isa extends Constraint {
        public Isa(com.typedb.driver.jni.ConstraintWithSpan nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isIsa() {
            return true;
        }

        @Override
        public Isa asIsa() {
            return this;
        }

        public ConstraintVertex instance() {
            return new ConstraintVertex(typedb_driver.constraint_isa_get_instance(nativeObject));
        }

        public ConstraintVertex type() {
            return new ConstraintVertex(typedb_driver.constraint_isa_get_type(nativeObject));
        }

        public com.typedb.driver.jni.ConstraintExactness exactness() {
            return typedb_driver.constraint_isa_get_exactness(nativeObject);
        }
    }

    public static class Has extends Constraint {
        public Has(com.typedb.driver.jni.ConstraintWithSpan nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isHas() {
            return true;
        }

        @Override
        public Has asHas() {
            return this;
        }

        public ConstraintVertex owner() {
            return new ConstraintVertex(typedb_driver.constraint_has_get_owner(nativeObject));
        }

        public ConstraintVertex attribute() {
            return new ConstraintVertex(typedb_driver.constraint_has_get_attribute(nativeObject));
        }

        public com.typedb.driver.jni.ConstraintExactness exactness() {
            return typedb_driver.constraint_has_get_exactness(nativeObject);
        }
    }

    public static class Links extends Constraint {
        public Links(com.typedb.driver.jni.ConstraintWithSpan nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isLinks() {
            return true;
        }

        @Override
        public Links asLinks() {
            return this;
        }

        public ConstraintVertex relation() {
            return new ConstraintVertex(typedb_driver.constraint_links_get_relation(nativeObject));
        }

        public ConstraintVertex player() {
            return new ConstraintVertex(typedb_driver.constraint_links_get_player(nativeObject));
        }

        public ConstraintVertex role() {
            return new ConstraintVertex(typedb_driver.constraint_links_get_role(nativeObject));
        }

        public com.typedb.driver.jni.ConstraintExactness exactness() {
            return typedb_driver.constraint_links_get_exactness(nativeObject);
        }
    }

    public static class Sub extends Constraint {
        public Sub(com.typedb.driver.jni.ConstraintWithSpan nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isSub() {
            return true;
        }

        @Override
        public Sub asSub() {
            return this;
        }

        public ConstraintVertex subtype() {
            return new ConstraintVertex(typedb_driver.constraint_sub_get_subtype(nativeObject));
        }

        public ConstraintVertex supertype() {
            return new ConstraintVertex(typedb_driver.constraint_sub_get_supertype(nativeObject));
        }

        public com.typedb.driver.jni.ConstraintExactness exactness() {
            return typedb_driver.constraint_sub_get_exactness(nativeObject);
        }
    }

    public static class Owns extends Constraint {
        public Owns(com.typedb.driver.jni.ConstraintWithSpan nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isOwns() {
            return true;
        }

        @Override
        public Owns asOwns() {
            return this;
        }

        public ConstraintVertex owner() {
            return new ConstraintVertex(typedb_driver.constraint_owns_get_owner(nativeObject));
        }

        public ConstraintVertex attribute() {
            return new ConstraintVertex(typedb_driver.constraint_owns_get_attribute(nativeObject));
        }

        public com.typedb.driver.jni.ConstraintExactness exactness() {
            return typedb_driver.constraint_owns_get_exactness(nativeObject);
        }
    }

    public static class Relates extends Constraint {
        public Relates(com.typedb.driver.jni.ConstraintWithSpan nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isRelates() {
            return true;
        }

        @Override
        public Relates asRelates() {
            return this;
        }

        public ConstraintVertex relation() {
            return new ConstraintVertex(typedb_driver.constraint_relates_get_relation(nativeObject));
        }

        public ConstraintVertex role() {
            return new ConstraintVertex(typedb_driver.constraint_relates_get_role(nativeObject));
        }

        public com.typedb.driver.jni.ConstraintExactness exactness() {
            return typedb_driver.constraint_relates_get_exactness(nativeObject);
        }
    }

    public static class Plays extends Constraint {
        public Plays(com.typedb.driver.jni.ConstraintWithSpan nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isPlays() {
            return true;
        }

        @Override
        public Plays asPlays() {
            return this;
        }

        public ConstraintVertex player() {
            return new ConstraintVertex(typedb_driver.constraint_plays_get_player(nativeObject));
        }

        public ConstraintVertex role() {
            return new ConstraintVertex(typedb_driver.constraint_plays_get_role(nativeObject));
        }

        public com.typedb.driver.jni.ConstraintExactness exactness() {
            return typedb_driver.constraint_plays_get_exactness(nativeObject);
        }
    }

    public static class FunctionCall extends Constraint {
        public FunctionCall(com.typedb.driver.jni.ConstraintWithSpan nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isFunctionCall() {
            return true;
        }

        @Override
        public FunctionCall asFunctionCall() {
            return this;
        }

        public String name() {
            return typedb_driver.constraint_function_call_get_name(nativeObject);
        }

        public Stream<ConstraintVertex> arguments() {
            return new NativeIterator<>(typedb_driver.constraint_function_call_get_arguments(nativeObject)).stream().map(ConstraintVertex::new);
        }

        public Stream<ConstraintVertex> assigned() {
            return new NativeIterator<>(typedb_driver.constraint_function_call_get_assigned(nativeObject)).stream().map(ConstraintVertex::new);
        }
    }

    public static class Expression extends Constraint {
        public Expression(com.typedb.driver.jni.ConstraintWithSpan nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isExpression() {
            return true;
        }

        @Override
        public Expression asExpression() {
            return this;
        }

        public String text() {
            return typedb_driver.constraint_expression_get_text(nativeObject);
        }

        public Stream<ConstraintVertex> arguments() {
            return new NativeIterator<>(typedb_driver.constraint_expression_get_arguments(nativeObject)).stream().map(ConstraintVertex::new);
        }

        public ConstraintVertex assigned() {
            return new ConstraintVertex(typedb_driver.constraint_expression_get_assigned(nativeObject));
        }
    }

    public static class Is extends Constraint {
        public Is(com.typedb.driver.jni.ConstraintWithSpan nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isIs() {
            return true;
        }

        @Override
        public Is asIs() {
            return this;
        }

        public ConstraintVertex lhs() {
            return new ConstraintVertex(typedb_driver.constraint_is_get_lhs(nativeObject));
        }

        public ConstraintVertex rhs() {
            return new ConstraintVertex(typedb_driver.constraint_is_get_rhs(nativeObject));
        }
    }

    public static class Iid extends Constraint {
        public Iid(com.typedb.driver.jni.ConstraintWithSpan nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isIid() {
            return true;
        }

        @Override
        public Iid asIid() {
            return this;
        }

        public ConstraintVertex variable() {
            return new ConstraintVertex(typedb_driver.constraint_iid_get_variable(nativeObject));
        }

        public String iid() {
            return typedb_driver.constraint_iid_get_iid(nativeObject);
        }
    }

    public static class Comparison extends Constraint {
        public Comparison(com.typedb.driver.jni.ConstraintWithSpan nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isComparison() {
            return true;
        }

        @Override
        public Comparison asComparison() {
            return this;
        }

        public ConstraintVertex lhs() {
            return new ConstraintVertex(typedb_driver.constraint_comparison_get_lhs(nativeObject));
        }

        public ConstraintVertex rhs() {
            return new ConstraintVertex(typedb_driver.constraint_comparison_get_rhs(nativeObject));
        }

        public com.typedb.driver.jni.Comparator comparator() {
            return typedb_driver.constraint_comparison_get_comparator(nativeObject);
        }

        public static String comparatorName(com.typedb.driver.jni.Comparator comparator) {
            return typedb_driver.comparator_get_name(comparator);
        }

    }

    public static class Kind extends Constraint {
        public Kind(com.typedb.driver.jni.ConstraintWithSpan nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isKindOf() {
            return true;
        }

        @Override
        public Kind asKindOf() {
            return this;
        }

        public com.typedb.driver.jni.Kind kind() {
            return typedb_driver.constraint_kind_get_kind(nativeObject);
        }

        public ConstraintVertex type() {
            return new ConstraintVertex(typedb_driver.constraint_kind_get_type(nativeObject));
        }
    }

    public static class Label extends Constraint {
        public Label(com.typedb.driver.jni.ConstraintWithSpan nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isLabel() {
            return true;
        }

        @Override
        public Label asLabel() {
            return this;
        }

        public ConstraintVertex variable() {
            return new ConstraintVertex(typedb_driver.constraint_label_get_variable(nativeObject));
        }

        public String label() {
            return typedb_driver.constraint_label_get_label(nativeObject);
        }
    }

    public static class Value extends Constraint {
        public Value(com.typedb.driver.jni.ConstraintWithSpan nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isValue() {
            return true;
        }

        @Override
        public Value asValue() {
            return this;
        }

        public ConstraintVertex attributeType() {
            return new ConstraintVertex(typedb_driver.constraint_value_get_attribute_type(nativeObject));
        }

        public String valueType() {
            return typedb_driver.constraint_value_get_value_type(nativeObject);
        }
    }

    public static class Or extends Constraint {
        public Or(com.typedb.driver.jni.ConstraintWithSpan nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isOr() {
            return true;
        }

        @Override
        public Or asOr() {
            return this;
        }

        public Stream<ConjunctionID> branches() {
            return new NativeIterator<>(typedb_driver.constraint_or_get_branches(nativeObject)).stream().map(ConjunctionID::new);
        }
    }

    public static class Not extends Constraint {
        public Not(com.typedb.driver.jni.ConstraintWithSpan nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isNot() {
            return true;
        }

        @Override
        public Not asNot() {
            return this;
        }

        public ConjunctionID conjunction() {
            return new ConjunctionID(typedb_driver.constraint_not_get_conjunction(nativeObject));
        }
    }

    public static class Try extends Constraint {
        public Try(com.typedb.driver.jni.ConstraintWithSpan nativeObject) {
            super(nativeObject);
        }

        @Override
        public boolean isTry() {
            return true;
        }

        @Override
        public Try asTry() {
            return this;
        }

        public ConjunctionID conjunction() {
            return new ConjunctionID(typedb_driver.constraint_try_get_conjunction(nativeObject));
        }
    }
}
