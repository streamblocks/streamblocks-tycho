
network Top (w = 20, h = 20) ==> :

entities 
	gol = GoL(w = w, h = h, init = a, nSteps = 1);
	m = Mapper(zoom = zoom);
	d = Display(title = "Game of Life", width = w * zoom, height = h * zoom, autoUpdate = zoom * zoom);
	
structure
	gol.Display --> m.In;
	m.X --> d.X;
	m.Y --> d.Y;
	m.R --> d.R;
	m.G --> d.G;
	m.B --> d.B;

var
	a = randomize(w, h);
	
	function randomize (w, h) :
		[
			[randomInt(2) : for j in 1..w] :
			for i in 1..h
		]
	end
	
	zoom = 20;

end	