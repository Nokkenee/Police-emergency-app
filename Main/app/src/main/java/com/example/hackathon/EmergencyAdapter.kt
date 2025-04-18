import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.compose.ui.text.font.emptyCacheFontFamilyResolver
import androidx.recyclerview.widget.RecyclerView
import com.example.hackathon.R

class EmergencyAdapter(
    private val emergencies: List<Emergency>,
    private val onAcceptClick: (Emergency) -> Unit
) : RecyclerView.Adapter<EmergencyAdapter.EmergencyViewHolder>() {

    class EmergencyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textName: TextView = view.findViewById(R.id.nameTextView)
        val idNumber: TextView = view.findViewById(R.id.idNumberTextView)
        val textAddress: TextView = view.findViewById(R.id.addressTextView)
        val textDetails: TextView = view.findViewById(R.id.txtEmergencyDetails)
        val btnAccept: Button = view.findViewById(R.id.btnAcceptReport)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmergencyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_emergency, parent, false)
        return EmergencyViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmergencyViewHolder, position: Int) {
        val emergency = emergencies[position]
        holder.textName.text = emergency.fullName
        holder.idNumber.text = emergency.idNumber
        holder.textAddress.text = emergency.address
        holder.textDetails.text = emergency.details
        holder.btnAccept.setOnClickListener {
            onAcceptClick(emergency)
        }
    }

    override fun getItemCount(): Int = emergencies.size
}
