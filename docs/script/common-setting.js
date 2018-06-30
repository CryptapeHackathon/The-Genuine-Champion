// this file is NOT for customization
// these are the default settings for all the Nervos Documents
// you can overwrite settings in this page by set them again in customization.js

var common = {

    loadSidebar: true,
    autoHeader: true,
    subMaxLevel: 2,
    loadNavbar: true,
    basePath: './',

    coverpage: true,

    // search: 'auto',

    // the default 
    alias: {
        '/_sidebar.md': `/${default_language}/_sidebar.md`,
    },

    // configuration for searching plugin
    search: {
        maxAge: 86400000, // expiration time in milliseconds, one day by default
        // paths: [
        // '/',
        // ], // or 'auto'

        // localization
        placeholder: {
            '/': '搜索',
        },


        // localization
        noData: {
            '/': '找不到结果',
        },

        // depth of the maximum searching title levels
        depth: 6,
    },


}