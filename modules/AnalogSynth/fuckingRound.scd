(
    w = Window("NumberBox Example", Rect(100, 500, 400, 120));
    b =     NumberBox(w, Rect(150, 10, 100, 20));
b.background_(Color.blue(1, 0.2));
b.normalColor_(Color.white);
    b.value = rrand(1,15);
    b.action = {arg numb; b.background_(Color.blue(1, numb.value.linexp(1,15,0.1,1)))};
    w.front
)

cablesUsed

a = Bag.new(0)

a.add(5)
a.remove(4)

a.itemCount(4)

a.countsDo{arg item; item.postln}


(
var colors = [Color.blue, Color.red, Color.green];
a = Window.new;

b = Button();

b.states = Array.fill(3, { arg i;
    [i.asString, Color.white, colors.wrapAt(i)]
});

b.action = { arg butt;
	d.postln;
	butt.value.postln;
	d[(butt.value-1).wrap(0, 2)].visible = false;
	d[butt.value].visible = true;
	a.layout = VLayout(b, d[butt.value]);

};
StackLayout
c = Array.fill(3, {Array.fill(5, {Slider()})});

d = List.newClear(0);
3.do{arg i;
	d.add(View().layout_(HLayout(*c[i])))
};

a.layout_(VLayout(b, d[0]));

a.front;
)
archive


a = [1,2,3,4,5,6,7];

b = {	a.size.asInteger};

b.value

Scoping















