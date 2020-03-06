%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% PVS_matched.m
% Written by Ravi Kedarasetti, 
% 
% Calculate patricle trajectories and mean flow speed 
% in a 3D model of the PVS with flow velocity matching the experimental results 
% Generates Fig 4e,4f,4g
% To be used for the output of PVS_3D_matched_p.class
%
% Written by Ravi Kedarasetti (2020), department of Engineering Science and
% Mechanics, Pennsylvania State University 
%% Problem parameters
clear all
close all
clc
Ro = 30E-6; %m
Lo = Ro;
c = 1;%m/s
f = 60/7; %Hz
tau = 1/f; % s
lam = c/f;
La = 5e-3;%m
g3fac = 10;
g1 = 1;
g2 = 1;
g3 = La/Lo/g3fac;
uo =Ro/100;% percent radius
pwd = 40E-6;
R1 = Ro+pwd;
disp_scale_r =  uo/Lo/g1;
disp_scale_z =  uo/Lo/g3;
%%
tstep = tau/100; %s
reps = 5;
tstop = reps*tau;
nd = 3; % formulation dimensions
dataFileList = dir('pvsMatchedResultsP*.txt');
nFiles = length(dataFileList);
p = zeros(1,nFiles);
mean_vel = zeros(1,nFiles);
for n = 1:nFiles
    %% read the data
    FileName =dataFileList(n).name;
    the_data = dlmread(FileName);
    nr = 21; % no of points in r
    nz = 101; % no of points in z
    p(n) = 0.01*str2double(FileName(19:end-4));
    %% Create a r-z grid
    rgrid = the_data(1:nr,1);
    zgrid = the_data(1:nr:end,nd-2+2);
    [Rgrid, Zgrid] = meshgrid(rgrid, zgrid);
    n_frames = tau/tstep;
    %% Read displacement data
    umr_data = reshape(the_data(:,4:6:end),nr,nz,n_frames);
    umr_data = repmat(umr_data,1,1,reps);
    umz_data = reshape(the_data(:,6:6:end),nr,nz,n_frames);
    umz_data = repmat(umz_data,1,1,reps);
    %% Read particle velocities
    xcdotr_data = reshape(the_data(:, 7:6:end),nr,nz,n_frames);
    xcdotr_data = repmat(xcdotr_data,1,1,reps);
    xcdotz_data = reshape(the_data(:, 9:6:end),nr,nz,n_frames);
    xcdotz_data = repmat(xcdotz_data,1,1,reps);
    n_frames = n_frames*reps;
    %% Particle trajectory for single particle
    dist_from_end = 1e-3;%m
    z_coord = zgrid(end) - dist_from_end/Lo/g3;
    r_wall = 1+0.03;
    r_mid = 1.5;
    xwall = forward_euler_2d([r_wall, z_coord], Rgrid,Zgrid, umr_data, umz_data,xcdotr_data, xcdotz_data,tstep,uo,disp_scale_r, disp_scale_z);
    xmid = forward_euler_2d([r_mid, z_coord], Rgrid,Zgrid, umr_data, umz_data,xcdotr_data, xcdotz_data,tstep,uo,disp_scale_r, disp_scale_z);
    
    t = (1:n_frames)*tstep;
    wall_disp_perc = 100*(xwall(:,1)-1);
    part_pos_um = 1e6*Lo*g3*(xmid(:,2) - xmid(1,2));
    net_disp = part_pos_um(end);
    mean_vel(n) = net_disp/(t(end) - t(1));
    
    
    if p(n)==0
        fig = figure();
        set(fig, 'Position', [1 1 1200 800])
        plot(t,part_pos_um,'b', 'LineWidth',2)
        ylabel('Particle displacement (\mum)')
        ax = gca;
        scl = diff(ax.YLim)/diff(ax.XLim);
        hold on
        plot([t(1) t(end)], [part_pos_um(1) part_pos_um(end)],'b')
        h = text(mean([t(1) t(end)]), mean([part_pos_um(1) part_pos_um(end)])- 0.025*diff(ax.YLim), ['Mean speed ' num2str(round(mean_vel(n),3)) '\mum/s'],...
            'FontSize' ,20,'HorizontalAlignment', 'center');
        m1 = pbaspect;
        m = atand(m1(2)*mean_vel(n)/scl);
        set(h, 'Rotation',m)
        set(gca,'YColor','b')
        set(gca, 'FontSize',14)
        xlabel('time(s)')
        saveas(fig, 'Fig4f.png')
        saveas(fig, 'Fig4f.pdf')
        %% Particle velocity for single particle
        wall_vel = 1e6*Lo*g1*diff(xwall(:,1))/tstep;
        part_vel = 1e6*Lo*g3*diff(xmid(:,2))/tstep;
        lim1 = max(wall_vel(1:2*tau/tstep));
        offset = 100;
        fig = figure();
        set(fig, 'Position', [1 1 1200 800])
        set(fig, 'Color', 'w')
        plot((t(1:2*tau/tstep) - t(offset))*f,wall_vel(1:2*tau/tstep), 'k', 'LineWidth',2)
        ylabel('Wall velocity (\mum/s)')
        ylim([-15 30])
        yyaxis right
        plot((t(1:2*tau/tstep) - t(offset))*f,part_vel(1:2*tau/tstep),'b', 'LineWidth',2)
        ylim([-max(part_vel(1:2*tau/tstep)) 1.2*max(part_vel(1:2*tau/tstep))])
        ylabel('Particle velocity (\mum/s)')
        xlabel('fractional cycle')
        xlim([0 1])
        ylim([-10 30])
        set(gca,'YColor','b')
        set(gca, 'FontSize',24)
        saveas(fig, 'Fig4e.png')
        saveas(fig, 'Fig4e.pdf')
    end
end
%% Plot mean speed velocities for all pressure values
fig = figure();
set(fig, 'Position', [1 1 1200 800])
set(fig, 'Color', 'w')
plot(p, mean_vel, 'b^-', 'LineWidth', 2, 'MarkerFaceColor', 'b', 'MarkerSize', 15)
xlabel('Pressue difference mmHg')
ylabel('Mean flow speed \mu m/s')
set(gca, 'FontSize', 24)
saveas(fig, 'Fig4g.png')
saveas(fig, 'Fig4g.pdf')


