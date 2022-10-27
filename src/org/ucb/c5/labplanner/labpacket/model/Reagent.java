package org.ucb.c5.labplanner.labpacket.model;

/**
 * Augmented from Transcriptic's Autoprotocol notion of Resource
 *
 * @author J. Christopher Anderson
 */
public enum Reagent {
    mastermix,
    ddH2O, //ie, high quality water
    
    //Enzymes
    Phusion,
    Q5_polymerase,
    PrimeSTAR_GXL_DNA_Polymerase,
    DpnI,
    BamHI,
    BglII,
    BsaI,
    BsmBI,
    T4_DNA_ligase,
    SpeI,
    XhoI,
    XbaI,
    PstI,
    Hindiii,
    
    //Buffers
    T4_DNA_Ligase_Buffer_10x,
    NEB_Buffer_2_10x,
    NEB_Buffer_3_10x,
    NEB_Buffer_4_10x,
    Q5_Polymerase_Buffer_5x,
    dNTPs_2mM,
    PrimeSTAR_GXL_Buffer_5x,
    PrimeSTAR_dNTP_Mixture_2p5mM,
    
    //Abstractions
    primer1,
    primer2,
    template,
    frag1,
    frag2,
    frag3,
    frag4,
    
    //Competent cells
    zymo_10b,
    Zymo_5a,
    JM109,
    DH10B,
    MC1061,
    Ec100D_pir116,
    Ec100D_pir_plus,
    
    //Agar Growth media
    lb_agar_50ug_ml_kan,
    lb_agar_100ug_ml_amp,
    lb_agar_100ug_ml_specto,
    lb_agar_100ug_ml_cm,
    lb_agar_noAB,
    
    //Liquid Growth media
    arabinose_10p,
    lb_specto,
    lb_amp,
    lb_kan,
    lb_cam,
    lb
}

