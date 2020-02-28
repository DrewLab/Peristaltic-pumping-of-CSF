function xp = forward_euler_2d(xo,R,Z,umr,umz,xcdotr, xcdotz, tstep,uo, disp_scale_r, disp_scale_z)
n_frames = size(umr,3);
xp = zeros(n_frames,2);
xc = zeros(n_frames,2);
xc(1,:) = xo;
xp(1,1) = xc(1,1) + disp_scale_r*interp2(R, Z, umr(:,:,1)', xc(1,1), xc(1,2), 'linear');
xp(1,2) = xc(1,2) + disp_scale_z*interp2(R, Z, umz(:,:,1)', xc(1,1), xc(1,2), 'linear');
zmax = max(Z(:));
for n = 1:n_frames-1
     xc(n+1,1) = xc(n,1) + (disp_scale_r/uo)*interp2(R, Z, xcdotr(:,:,n)', xc(n,1), xc(n,2), 'linear')*tstep;
     xc(n+1,2) = xc(n,2) + (disp_scale_z/uo)*interp2(R, Z, xcdotz(:,:,n)', xc(n,1), xc(n,2), 'linear')*tstep;
     xp(n+1,1) = xc(n+1,1) + disp_scale_r*interp2(R, Z, umr(:,:,n+1)', xc(n+1,1), xc(n+1,2), 'linear');
     xp(n+1,2) = xc(n+1,2) + disp_scale_z*interp2(R, Z, umz(:,:,n+1)', xc(n+1,1), xc(n+1,2), 'linear');  
     

%      
     if xp(n+1,2) > zmax
         xp(n+2:end,1) = nan;
         xp(n+2:end,2) = nan;
         break
     end
end