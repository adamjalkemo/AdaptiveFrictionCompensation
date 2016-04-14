function [ theta ] = RLS(pendAng,control,phidot)
%RLS Summary of this function goes here
%   Detailed explanation goes here
    persistent P_old;
    persistent theta_old;
    persistent phi;
    pam=100;
    pbm=100;
    aguess=0;
    bguess=0;
    if(isempty(P_old))
        P_old=[pam 0;0 pbm];
        theta_old = [aguess bguess]';
        phi = [0 0]';
        pendAng_old = 0;
        control_old = 0;
    end
    P_old = P_old - (P_old*phi*phi' *P_old)/(1+phi'*P_old*phi);
    VL = phiDot-0.0059*pendAnd_old-1.9125*control_old-phi(1);
    epsilon = VL - phi'*theta_old;
    theta_old = theta_old + P_old*phi.*epsilon;
    phi = [phidot sign(phidot)]';
    pendAng_old = pendAng;
    control_old = control;
    theta = theta_old;
end

