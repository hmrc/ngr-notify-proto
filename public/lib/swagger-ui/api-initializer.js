window.onload = function () {
    window.ui = SwaggerUIBundle({
        urls: [
            {
                "url": "/assets/api-ngr-notify-1.0.0.yaml",
                "name": "API-NGR-notify"
            }
        ],
        dom_id: '#swagger-ui',
        deepLinking: true,
        presets: [
            SwaggerUIBundle.presets.apis,
            SwaggerUIStandalonePreset
        ],
        plugins: [
            SwaggerUIBundle.plugins.DownloadUrl
        ],
        layout: "StandaloneLayout"
    });
};
