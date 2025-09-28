# Koverage

**Koverage** is a Kotlin-based simulator for modelling and evaluating **network coverage** in industrial environments using **mobile sinks**, such as service vehicles, autonomous ground vehicles, or aerial drones.  

It supports a modular architecture to simulate various mobility behaviours and analyse wireless sensor network (WSN) coverage over time.

---

## 🚗 Supported Mobility Models

Koverage currently supports three mobility models:

- **Random Waypoint** [Bettstetter, 2001]  
- **Random Direction** [Royer, 2001]  
- **Specialised Synthetic Model** [Amiri et al., 2024]  

📌 The *Specialised Synthetic Model* is a custom mobility model based on real-world trace data, designed to reflect structured vehicular motion in constrained environments such as industrial yards or logistics zones.

This model was presented at the **2024 International Telecommunication Networks and Applications Conference (ITNAC):**

```bibtex
@INPROCEEDINGS{10815387,
  author={Amiri, Max and Eyers, David and Huang, Zhiyi},
  booktitle={2024 34th International Telecommunication Networks and Applications Conference (ITNAC)}, 
  title={A Specialised Synthetic Mobility Model Based on Real-World Traces}, 
  year={2024},
  pages={1--5},
  doi={10.1109/ITNAC62915.2024.10815387}
}
```

---

## 🎓 Research Context

Koverage has been developed as part of ongoing PhD research by **Max Amiri**.  
It complements the **NTL – Network Time Link** prototype by providing coverage evaluation and simulation insights for mobility-assisted IoT communication.

The concept has been published and can be cited as:

> **Energy-Efficient Communication and Localisation Protocol for Industrial IoT Devices**  
> Max Amiri, David Eyers, and Morteza Biglari-Abhari  
> In *Proceedings of the 9th IEEE International Conference on Smart Internet of Things (SmartIoT 2025)*,  
> 17–20 November 2025, Sydney, Australia.

### 📑 BibTeX Citation

```bibtex
@inproceedings{Amiri2025NTL,
  title={Energy-Efficient Communication and Localisation Protocol for Industrial IoT Devices},
  author={Amiri, Max and Eyers, David and Biglari-Abhari, Morteza},
  booktitle={Proceedings of the 9th IEEE International Conference on Smart Internet of Things (SmartIoT)},
  year={2025},
  month={November},
  address={Sydney, Australia}
}
```

---

## 📬 Contact

For collaboration opportunities, please reach out to **Max Amiri**.

---

> Made with Kotlin. Built for industrial IoT research.
