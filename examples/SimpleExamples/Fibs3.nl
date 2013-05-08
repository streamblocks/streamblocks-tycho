network Fibs3 () ==> Out:

entities
  init1 = InitialTokens(tokens = [1]);
  init2 = InitialTokens(tokens = [1]);
  init3 = InitialTokens(tokens = [1]);
  add1 = Add();
  add2 = Add();
structure
  init1.Out --> init2.In;
  init2.Out --> init3.In;

  init1.Out --> add1.A;
  init2.Out --> add1.B;

  add1.Result --> add2.A;
  init3.Out --> add2.B;

  add2.Result --> init1.In;
  add2.Result --> Out;
end

