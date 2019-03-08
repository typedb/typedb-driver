const GraknClient = require("grakn-client");


beforeEach(async () => {
    client = await GraknClient("localhost:48555");
    session = await client.session("testkeyspace");
    tx = await session.transaction().write();
})

afterEach(() => {
    await tx.close();
    await session.close();
    client.close()
});




describe("Basic GraknClient Tests", () => {

    test("define", async () => {
        const defined = await tx.query("define person sub entity, has name; name sub attribute, datatype string;");
        await tx.commit()
    });

    test("match", async () => {
        const types = await tx.query("match $x sub thing; get;");
    });

    test("insert", async () => {
        const defined = await tx.query("define person sub entity, has name; name sub attribute, datatype string;");
        const inserted = await tx.query("insert $x isa person, has name \"john\";");
    });
});


