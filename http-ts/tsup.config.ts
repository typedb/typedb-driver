import type { Options } from "tsup";

const env = process.env.NODE_ENV;

const tsup: Options = {
    splitting: true,
    sourcemap: true,
    clean: true,
    dts: true,
    format: ["cjs", "esm"],
    outExtension({ format }) {
        return {
            js: format === "esm" ? ".mjs" : ".cjs",
        };
    },
    minify: false,
    bundle: true,
    skipNodeModulesBundle: true,
    watch: false,
    target: "es2020",
    outDir: "dist",
    entry: ["src/index.ts"],
};

export default tsup;
