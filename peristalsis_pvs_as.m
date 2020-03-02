%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% peristalsis_pvs_as.m
% Written by Ravi Kedarasetti, 
% 
% Calculate patricle trajectories and mean flow speed to oscillatory
% velocity ratio in an axisymmetric model of the PVS for different
% amplitudes of pulse waves.
% 
% To be used for the output of Peristalsis_as_amp_sweep.class
%
% Written by Ravi Kedarasetti (2020), department of Engineering Science and
% Mechanics, Pennsylvania State University 
%% Problem parameters
Lo = 30e-6;%m
c = 1;%m/s
f = 60/7; %Hz
tau = 1/f; % s
lam = c/f;
La = lam;%m
g1 = 1;
g3 = lam/Lo/10;
tstep = tau/50; %s
reps = 10;
tstop = reps*tau;
t_vec = (1:100*reps)*tstep;
uo = 2.^(-2:-1:-8)*Lo;
vo = c*uo/Lo;
%% read the data
FileName = 'axisymmetricResults.txt';
phi = uo/Lo;
pump_to_osc = zeros(size(phi));
the_data = dlmread(FileName);

%% Create a grid
nr = 21;
nz = 101;
rgrid = the_data(1:nr,1);
zgrid = the_data(1:nr:end,2);

zreps =4;
zgrid1= zgrid;

%% extend the grid to track particles that move to the next periodic segment
for n = 1:zreps-1
    zgrid1 = [zgrid1 ; (zgrid1(end) + zgrid(2:end))];
end
[Rgrid, Zgrid] = meshgrid(rgrid, zgrid1);
%% Particle tracking grid
partgrid_r = mean(rgrid);
partgrid_z = linspace(min(zgrid),max(zgrid),12);%mean(zgrid);
[partgrid_R, partgrid_Z] = meshgrid(partgrid_r, partgrid_z(2:end-1));
partgrid_R = partgrid_R(:);
partgrid_Z = partgrid_Z(:);
%% Calculate fluid particle trajectories for each phi value
for n1 = 1:length(uo)
    n_frames = 2*tau/tstep; %2 cycles of data
    
    umr_data = reshape(the_data(:, 3 +3*(n1-1)*n_frames:3:3*n1*n_frames),nr,nz,n_frames);
    umr_data = repmat(umr_data,1,1,reps);
    
    xcdotr_data = reshape(the_data(:, 4 +3*(n1-1)*n_frames:3:3*n1*n_frames+1),nr,nz,n_frames);
    xcdotr_data = repmat(xcdotr_data,1,1,reps);
    
    xcdotz_data = reshape(the_data(:, 5 +3*(n1-1)*n_frames:3:3*n1*n_frames+2),nr,nz,n_frames);
    xcdotz_data = repmat(xcdotz_data,1,1,reps);
    
    n_frames = n_frames*reps;
    umr_data1 = umr_data;
    xcdotr_data1 = xcdotr_data;
    xcdotz_data1 = xcdotz_data;
    
    for n = 1:zreps-1
        umr_data1 = [umr_data1  umr_data(:,2:end,:)];
        xcdotr_data1 = [xcdotr_data1  xcdotr_data(:,2:end,:)];
        xcdotz_data1 = [xcdotz_data1  xcdotz_data(:,2:end,:)];
    end
    
    umr_data = umr_data1;
    xcdotr_data = xcdotr_data1;
    xcdotz_data = xcdotz_data1;
    umz_data = zeros(size(umr_data));
    %% The non dimensionalized displacement is scaled differently for each phi value
    disp_scale_r =  uo(n1)/Lo/g1;
    disp_scale_z =  uo(n1)/Lo/g3;
    %% Perform forward euler time integration
    xm = zeros(n_frames, 2, length(partgrid_R));
    for n = 1:length(partgrid_R)
        xm(:,:,n) =forward_euler_2d([partgrid_R(n), partgrid_Z(n)], Rgrid,Zgrid, umr_data, umz_data,xcdotr_data, xcdotz_data,tstep,uo(n1),disp_scale_r, disp_scale_z);
    end
    %% Calculate mean flow speed and oscillatory velocity
    amps =zeros(1,length(partgrid_R));
    disps = zeros(1,length(partgrid_R));
    vPump =zeros(1,length(partgrid_R));
    vOsc = zeros(1,length(partgrid_R));

    for n = 1:length(partgrid_R)
        traj = squeeze(xm(:,2,n));
        vPump(n) = g3*Lo*(traj(end)- traj(1))/(tstep*n_frames);
        vOsc(n) = g3*Lo*(max(diff(traj)) - min(diff(traj)))/tstep - vPump(n);
    end
    
    pump_to_osc(n1) = mean(vPump)/mean(vOsc);
    %% Plot particle trajectories
    fig = figure();
    plot(t_vec, (traj - traj(1))*g3*Lo*1e6, 'LineWidth',2)
    hold on
    plot([t_vec(1), t_vec(end)], [0 traj(end) - traj(1)]*g3*Lo*1e6, 'k--')
    xlabel('Time(s)')
    ylabel('Displacement (\mum)')
    title(['Fluid particle displacement (\phi = ' num2str(phi(n1)*100,2) '%)']) 
    xlim([0 5/f])
    xends = get(gca, 'XLim');
    yends = get(gca, 'YLim');
    xends = xends(2) - xends(1);
    yends = yends(2) - yends(1);
    pb = pbaspect;
    ht = text( 0.5*(t_vec(n_frames*5/reps/2)), 0.5*(traj(n_frames*5/reps/2)- traj(1))*g3*Lo*1e6 - yends/10,['Mean flow speed : ' num2str(round(vPump(n)*1e6,1)) '\mum/s'], 'HorizontalAlignment', 'center' );
    set(ht, 'FontSize', 15)
    set(ht, 'Rotation', rad2deg(atan(pb(2)/pb(1)*xends/yends*(traj(end) - traj(1))*g3*Lo*1e6/(t_vec(n_frames)))))
    set(gca, 'FontSize', 11)
    saveas(fig,[ FileName(1:end-4) 'phi_' num2str(phi(n1)*100,2) 'perc_traj.pdf'])
    saveas(fig,[ FileName(1:end-4) 'phi_' num2str(phi(n1)*100,2) 'perc_traj.png'])
end
%% Plot mean flow speed to oscillatory velocity ratio
fig = figure();
loglog(phi*100, pump_to_osc,'-^','LineWidth',2, 'MarkerFaceColor', 'b', 'MarkerEdgeColor', 'none', 'MarkerSize', 10)
xlabel('\phi %')
title('Mean flow speed to oscillatory velocity ratio')
grid on
xlim([0.1 100])
ylim([0.001 1])
set(gca, 'FontSize', 12)
set(gca, 'GridAlpha',0.5)
saveas(fig,['ratio.pdf'])
saveas(fig,['ratio.png'])