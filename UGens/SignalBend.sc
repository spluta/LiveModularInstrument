// without mul and add.
//SignalBend is looking for a bend value between 0 and 1
SignalBend : UGen {
    *ar { arg val = 0.5, bend = 0.5;
        ^this.multiNew('audio', val, bend)
    }
}