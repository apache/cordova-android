var app = {
    initialize: function() {
        this.bind();
    },
    bind: function() {
        document.addEventListener('deviceready', this.deviceready, false);
    },
    deviceready: function() {
        app.report('deviceready');
    },
    report: function(id) {
        document.querySelector('#' + id + ' .pending').classList.add('hide');
        document.querySelector('#' + id + ' .complete').classList.remove('hide');
    }
};
