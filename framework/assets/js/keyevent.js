function KeyEvent() 
{
}

KeyEvent.prototype.backTrigger = function()
{
  var e = document.createEvent('Events');
  e.initEvent('backKeyDown');
  document.dispatchEvent(e);
}

if (document.keyEvent == null || typeof document.keyEvent == 'undefined')
{
  window.keyEvent = document.keyEvent = new KeyEvent();
}
