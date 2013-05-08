
network ButterflyFFT (NSTAGES, scale) In ==> Out :

entities
	bf = Butterfly(NSTAGES = NSTAGES);
	scaler = ConstantMultiply(c = scale);
	
structure
	In --> bf.In;
	bf.Out --> scaler.In;
	scaler.Out --> Out;
end