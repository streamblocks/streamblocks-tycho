

network Butterfly (NSTAGES) In ==> Out :

var
	N2 = pow2(NSTAGES - 1);
	nBF = if NSTAGES > 1 then 2 else 0 end;
	
	function pow2(a) : 
		if a = 0 then 1 else 2 * pow2(a - 1) end
	end
	 
entities
	split = Split(N = N2);
	merge = Merge();
	twiddles = TwiddleGenerator(N = N2);
	r2cell = Radix2Cell();
	bf = [Butterfly(NSTAGES = NSTAGES - 1) : for i in 1 .. nBF];
	
structure
	In --> split.In;
	In --> twiddles.Trigger;
	merge.Out --> Out;
	
	split.A --> r2cell.X0;
	split.B --> r2cell.X1;
	twiddles.W --> r2cell.W;
	
	if NSTAGES > 1 then
		r2cell.Y0 --> bf[0].In;
		r2cell.Y1 --> bf[1].In;
		bf[0].Out --> merge.A;
		bf[1].Out --> merge.B;
	else
		r2cell.Y0 --> merge.A;
		r2cell.Y1 --> merge.B;
	end 
end
