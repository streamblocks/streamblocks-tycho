
network Top () ==> :

entities
	init = InitialTokens(tokens = [[.5, 1], [1, -1], [-0.1, 1], [-1, -1]]);
	sd = MLSD(N = 2, R = [[1, 0], [0, 1]], O = [-1, 1]);
	print = Print();
	
structure
	init.Out --> sd.Y;
	init.Out --> print.Y;
	sd.S --> print.S;
end 

