
network Sum () In ==> Out:

entities
  init0 = InitialTokens(tokens = [0]);
  init2 = InitialTokens(tokens = [0]);
  add = Add();

structure
  In --> add.A;
  init0.Out --> add.B;
  add.Result --> init0.In;
  add.Result --> Out;
end  