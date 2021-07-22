const CompressionPlugin = require("compression-webpack-plugin");
const zlib = require("zlib");

config.plugins.push(
	new CompressionPlugin({
		filename: "[path][base].br",
		algorithm: "brotliCompress",
		test: /\.js$/,
		compressionOptions: {
			params: {
				[zlib.constants.BROTLI_PARAM_QUALITY]: 11
			}
		}
	}),
	new CompressionPlugin({
		filename: "[path][base].gz",
		algorithm: "gzip",
		test: /\.js$/
	})
);
