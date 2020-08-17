package com.team2059.scouting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;

import org.team2059.scouting.core.Team;
import org.team2059.scouting.core.frc2020.IrTeam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class RecyclerViewAdapterTeam extends RecyclerView.Adapter<RecyclerViewAdapterTeam.ViewHolder> {

    private ArrayList<Team> teams;
    private Context context;

    private ViewHolderListener listener;

    public interface ViewHolderListener{
        void onTeamClick(int position, ImageView avatar, TextView teamName, TextView teamNumber);
    }

    public void setViewHolderListener(ViewHolderListener listener){
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView avatar;
        private TextView position;
        private TextView rank;
        private TextView record;
        private TextView teamName;
        private TextView teamNumber;
        private TextView attr1;
        private TextView attr2;

        public ViewHolder(@NonNull View itemView, final ViewHolderListener listener) {
            super(itemView);
            avatar = itemView.findViewById(R.id.team_card_avatar);
            position = itemView.findViewById(R.id.team_card_position);
            rank = itemView.findViewById(R.id.team_card_rank);
            record = itemView.findViewById(R.id.team_card_record);
            teamName = itemView.findViewById(R.id.team_card_name);
            teamNumber = itemView.findViewById(R.id.team_card_number);
            attr1 = itemView.findViewById(R.id.team_card_attr1);
            //attr2 = itemView.findViewById(R.id.team_card_attr2);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onTeamClick(position, avatar, teamName, teamNumber);
                        }
                    }
                }
            });

        }
    }

    public RecyclerViewAdapterTeam(ArrayList<Team> teams){
        this.teams = teams;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_team_card, parent, false);
        ViewHolder viewHolder = new ViewHolder(v, listener);
        context = parent.getContext();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Team team = teams.get(position);
        if(team instanceof IrTeam){
            IrTeam irTeam = (IrTeam) team;

            byte [] bytes = Base64.decode(irTeam.getbyteMapString(), Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            // in a recycler view holder may be "recycled" so a default value for background
            // color and image must be assigned to prevent the recycled background color or image from showing
            if(bytes.length > 0){
                holder.avatar.setImageBitmap(bmp);
                //holder.avatar.setBackgroundColor(context.getResources().getColor(R.color.frc_avatar_blue));
                holder.avatar.setBackgroundResource(R.drawable.avatar_background);
            }
            else{
                holder.avatar.setImageBitmap(null);
                holder.avatar.setBackgroundColor(Color.TRANSPARENT);
            }

            holder.position.setText(Integer.toString(position + 1));
            holder.rank.setText("Rank 1");
            holder.record.setText(Integer.toString(irTeam.getTotalPoints()));
            holder.teamName.setText(irTeam.getTeamName());
            holder.teamNumber.setText(irTeam.getTeamNumber());
            holder.attr1.setText("Auto Powercell Count: " + irTeam.getAutoPowercellCount());

            double opr = ((double) irTeam.getTotalPoints())/irTeam.getIrMatches().size();
            //round to 2 decimal places
            String roundopr = new BigDecimal(String.valueOf(opr)).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            holder.attr1.setText("OPR: " + roundopr);

            //holder.attr2.setText(Integer.toString(irTeam.getTeleopPowercellCount()));
        }

    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

}