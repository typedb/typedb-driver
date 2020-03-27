package grakn.client.concept;

public interface UserType<
        SomeType extends UserType<SomeType, SomeThing>,
        SomeThing extends Thing<SomeThing, SomeType>>
        extends Type<SomeType, SomeThing> {

}
