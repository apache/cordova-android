PhoneGap.Channel = function(type)
{
    this.type = type;
    this.handlers = {};
    this.guid = 0;
    this.fired = false;
    this.enabled = true;
};

PhoneGap.Channel.prototype.sub = function(f, c, g)
{
    // need a function to call
    if (f == null)
        return;

    var func = f;
    if (typeof c == "object" && f instanceof Function)
        func = PhoneGap.close(c, f);

    g = g || func.observer_guid || f.observer_guid || this.guid++;
    func.observer_guid = g;
    f.observer_guid = g;
    this.handlers[g] = func;
    return g;
};

PhoneGap.Channel.prototype.sob = function(f, c)
{
    var g = null;
    var _this = this;
    var m = function() {
        f.apply(c || null, arguments);
        _this.dub(g);
    }
    if (this.fired) {
	    if (typeof c == "object" && f instanceof Function)
	        f = PhoneGap.close(c, f);
        f.apply(this, this.fireArgs);
    } else {
        g = this.sub(m);
    }
    return g;
};

PhoneGap.Channel.prototype.dub = function(g)
{
    if (g instanceof Function)
        g = g.observer_guid;
    this.handlers[g] = null;
    delete this.handlers[g];
};

PhoneGap.Channel.prototype.fire = function(e)
{
    if (this.enabled)
    {
        var fail = false;
        for (var item in this.handlers) {
            var handler = this.handlers[item];
            if (handler instanceof Function) {
                var rv = (handler.apply(this, arguments)==false);
                fail = fail || rv;
            }
        }
        this.fired = true;
        this.fireArgs = arguments;
        return !fail;
    }
    return true;
};

PhoneGap.Channel.merge = function(h, e) {
       var i = e.length;
       var f = function() {
           if (!(--i)) h();
       }
       for (var j=0; j<i; j++) {
           (!e[j].fired?e[j].sob(f):i--);
       }
       if (!i) h();
}