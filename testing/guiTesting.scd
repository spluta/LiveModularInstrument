CompositeView

(
w = Window.new("bob");

w.userCanClose_(false);

c = CompositeView().maxWidth_(100).maxHeight_(100);
a = Slider2D(); // actually displays at (50, 0)
b = Slider2D();

c.background = Color.rand;

c.layout_(VLayout(a,b));

w.layout_(VLayout(c));

w.front;
)

(
u = List[DragSource().string_("100").font_(Font("Helvetica",10)).maxHeight_(10),
	Button.new.states_([["25"]]),	Button.new.states_([["42"]])];
v = CompositeView();
v.layout_(
	VLayout(*u).spacing_(0).margins_([0,0,0,0])).maxWidth_(20);

w = Window.new.layout_(
    HLayout(30,
		[v,align:\top],
        TextView(),
		[DragSink().string_("poop").align_(\center), align:\top]
	).spacing_(0).margins_([0,0,0,0])
).front;
)

u.removeAt(1)
v.layout_(VLayout(*u).spacing_(0).margins_([0,0,0,0])).maxWidth_(20);

v.layout.add(Button.new.states_([["Super"]]))
v.layout.remove
v.layout_()
w.layout_(HLayout(
		[v,align:\top],
        TextView(),
	[Button.new.states_([["Collider"]]), align:\top]))

GridLayout(nil)

Boolean

a = true;
a.not

ModularClassList('normal');
a = ClassWindow_Mod.makeWindow

a = InBusWindow_Mod.makeWindow

ClassWindow_Mod.win.visible

a.toggleVisible

(
v = Array.fill(16, {ModularObjectPanel()});


//w = Window.new.layout_(HLayout([u.panel, align:\topLeft]).margins_(0!4).spacing_(0)).front;
w = Window();
w.view.maxWidth_(320).maxHeight_(320);
w.layout_(GridLayout.rows(*v.collect({arg item; item.view}).clump(4)).margins_(0!4).spacing_(0));
w.userCanClose=false;
w.front;
)

a = AssignButton()
a.maxWidth_(20)
a.layout.maxWidth_(40)

(0..15).clump(4)

w.close
w.front.visible_(true).front

(
v = Array.fill(4, {ModularObjectPanel()});


//w = Window.new.layout_(HLayout([u.panel, align:\topLeft]).margins_(0!4).spacing_(0)).front;
w = Window.new.layout_(HLayout(*v.collect({arg item; item.view})).margins_(0!4).spacing_(0));
w.userCanClose=false;
w.front;
)
v[0].busAssignSink.assignBus(20, "poop");
v[0].busAssignSink.assignBus(21, 21);
v[0].busAssignSink.assignBus(22, 22);
v[0].busAssignSink.assignBus(23, 23);
v[0].busAssignSink.assignBus(25, 25);
v[0].busAssignSink.assignBus(26, 26);
u.assignBus(27, 27);
u.assignBus(2, 2);
OSCFunc.trace

"dogshit10".asInteger

(
x = Window.new.front;
x.alpha = 0.8;
x.onClose_({ y = Synth.new(\default) }); //close the window and the synth plays
x.resizeTo(100,100)
)
x.close;
y.free;